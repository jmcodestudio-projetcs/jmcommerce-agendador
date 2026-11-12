package br.com.jmcodestudio.agendador.agendamento;

import br.com.jmcodestudio.agendador.agenda.HorarioRepository;
import br.com.jmcodestudio.agendador.domain.Agendamento;
import br.com.jmcodestudio.agendador.domain.HorarioDisponivel;
import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import br.com.jmcodestudio.agendador.erro.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Acoes do fotografo sobre os pedidos recebidos. */
@Service
public class AgendamentoService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate SEM_DATA_FINAL = LocalDate.of(2999, 12, 31);
    private static final List<StatusAgendamento> STATUS_ATIVOS =
            List.of(StatusAgendamento.PENDENTE, StatusAgendamento.APROVADO);

    private final AgendamentoRepository agendamentoRepository;
    private final HorarioRepository horarioRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              HorarioRepository horarioRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.horarioRepository = horarioRepository;
    }

    /** Filtros opcionais. Sem datas, lista de hoje em diante; sem status, todos. */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listar(Long fotografoId, StatusAgendamento status,
                                            LocalDate de, LocalDate ate) {
        LocalDate inicio = de != null ? de : LocalDate.now(FUSO);
        LocalDate fim = ate != null ? ate : SEM_DATA_FINAL;
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "VALIDACAO",
                    "A data final não pode ser anterior à data inicial.");
        }
        List<StatusAgendamento> statusFiltro =
                status != null ? List.of(status) : List.of(StatusAgendamento.values());

        return agendamentoRepository.listarDoPainel(fotografoId, statusFiltro, inicio, fim).stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse ver(Long fotografoId, Long id) {
        return AgendamentoResponse.de(buscar(fotografoId, id));
    }

    @Transactional
    public AgendamentoResponse aprovar(Long fotografoId, Long id) {
        Agendamento agendamento = buscar(fotografoId, id);
        exigirPendente(agendamento, "aprovado");
        agendamento.setStatus(StatusAgendamento.APROVADO);
        return AgendamentoResponse.de(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse recusar(Long fotografoId, Long id, String motivo) {
        Agendamento agendamento = buscar(fotografoId, id);
        exigirPendente(agendamento, "recusado");
        agendamento.setStatus(StatusAgendamento.RECUSADO);
        agendamento.setMotivo(vazioParaNull(motivo));
        return AgendamentoResponse.de(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse cancelar(Long fotografoId, Long id, String motivo) {
        Agendamento agendamento = buscar(fotografoId, id);
        exigirAtivo(agendamento, "cancelado");
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivo(vazioParaNull(motivo));
        return AgendamentoResponse.de(agendamentoRepository.save(agendamento));
    }

    /** Troca o horario e mantem o status; o horario antigo volta a ficar livre. */
    @Transactional
    public AgendamentoResponse reagendar(Long fotografoId, Long id, Long novoHorarioId) {
        Agendamento agendamento = buscar(fotografoId, id);
        exigirAtivo(agendamento, "reagendado");

        HorarioDisponivel novoHorario = horarioRepository
                .findByIdAndFotografoId(novoHorarioId, fotografoId)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Horário não encontrado."));

        if (agendamentoRepository.existsByHorarioIdAndStatusIn(novoHorario.getId(), STATUS_ATIVOS)) {
            throw horarioIndisponivel();
        }

        agendamento.setHorario(novoHorario);
        agendamento.setData(novoHorario.getData());
        agendamento.setHora(novoHorario.getHora());
        try {
            // Mesma protecao do pedido: o indice unico recusa se o horario foi ocupado agora.
            agendamentoRepository.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException ex) {
            throw horarioIndisponivel();
        }
        return AgendamentoResponse.de(agendamento);
    }

    private Agendamento buscar(Long fotografoId, Long id) {
        return agendamentoRepository.findByIdAndFotografoId(id, fotografoId)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Agendamento não encontrado."));
    }

    private void exigirPendente(Agendamento agendamento, String acao) {
        if (agendamento.getStatus() != StatusAgendamento.PENDENTE) {
            throw transicaoInvalida("Só um agendamento pendente pode ser " + acao + ".");
        }
    }

    private void exigirAtivo(Agendamento agendamento, String acao) {
        if (!agendamento.getStatus().ativo()) {
            throw transicaoInvalida("Este agendamento já foi encerrado e não pode ser " + acao + ".");
        }
    }

    private static RegraNegocioException transicaoInvalida(String mensagem) {
        return RegraNegocioException.conflito("TRANSICAO_INVALIDA", mensagem);
    }

    private static RegraNegocioException horarioIndisponivel() {
        return RegraNegocioException.conflito("HORARIO_INDISPONIVEL",
                "Este horário já está ocupado. Escolha outro.");
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
