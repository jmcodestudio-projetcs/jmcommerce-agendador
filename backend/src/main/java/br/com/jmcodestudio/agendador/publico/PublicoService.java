package br.com.jmcodestudio.agendador.publico;

import br.com.jmcodestudio.agendador.agenda.HorarioRepository;
import br.com.jmcodestudio.agendador.agendamento.AgendamentoRepository;
import br.com.jmcodestudio.agendador.domain.Agendamento;
import br.com.jmcodestudio.agendador.domain.Fotografo;
import br.com.jmcodestudio.agendador.domain.HorarioDisponivel;
import br.com.jmcodestudio.agendador.domain.Servico;
import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import br.com.jmcodestudio.agendador.domain.StatusAssinatura;
import br.com.jmcodestudio.agendador.erro.RegraNegocioException;
import br.com.jmcodestudio.agendador.fotografo.FotografoRepository;
import br.com.jmcodestudio.agendador.servico.ServicoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/** Tudo o que o cliente final faz, sem login. */
@Service
public class PublicoService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int PERIODO_MAXIMO_DIAS = 366;
    private static final List<StatusAgendamento> STATUS_ATIVOS =
            List.of(StatusAgendamento.PENDENTE, StatusAgendamento.APROVADO);
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final FotografoRepository fotografoRepository;
    private final ServicoRepository servicoRepository;
    private final HorarioRepository horarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    public PublicoService(FotografoRepository fotografoRepository,
                          ServicoRepository servicoRepository,
                          HorarioRepository horarioRepository,
                          AgendamentoRepository agendamentoRepository) {
        this.fotografoRepository = fotografoRepository;
        this.servicoRepository = servicoRepository;
        this.horarioRepository = horarioRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional(readOnly = true)
    public PerfilPublicoResponse perfil(String slug) {
        Fotografo fotografo = fotografoDoLink(slug);
        List<PerfilPublicoResponse.ServicoPublico> servicos = servicoRepository
                .findByFotografoIdAndAtivoTrueOrderByNomeAsc(fotografo.getId()).stream()
                .map(s -> new PerfilPublicoResponse.ServicoPublico(s.getId(), s.getNome(),
                        s.getDescricao(), s.getDuracaoMinutos(),
                        s.isExibirPreco() ? s.getPreco() : null))
                .toList();
        return new PerfilPublicoResponse(fotografo.getNome(), fotografo.getBio(),
                fotografo.getTelefoneWhatsapp(), servicos);
    }

    /** Horarios livres e futuros do fotografo no periodo. */
    @Transactional(readOnly = true)
    public List<HorarioLivreResponse> horariosLivres(String slug, LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        Fotografo fotografo = fotografoDoLink(slug);
        Set<Long> ocupados = agendamentoRepository
                .idsDeHorariosOcupados(fotografo.getId(), STATUS_ATIVOS, de, ate);
        LocalDateTime agora = LocalDateTime.now(FUSO);

        return horarioRepository
                .findByFotografoIdAndDataBetweenOrderByDataAscHoraAsc(fotografo.getId(), de, ate)
                .stream()
                .filter(h -> !ocupados.contains(h.getId()))
                .filter(h -> LocalDateTime.of(h.getData(), h.getHora()).isAfter(agora))
                .map(h -> new HorarioLivreResponse(h.getId(), h.getData(), h.getHora()))
                .toList();
    }

    @Transactional
    public AgendamentoPublicoResponse criarPedido(String slug, PedidoRequest req) {
        Fotografo fotografo = fotografoDoLink(slug);

        Servico servico = servicoRepository
                .findByIdAndFotografoId(req.servicoId(), fotografo.getId())
                .filter(Servico::isAtivo)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Serviço não encontrado."));

        HorarioDisponivel horario = horarioRepository
                .findByIdAndFotografoId(req.horarioId(), fotografo.getId())
                .orElseThrow(PublicoService::horarioIndisponivel);

        boolean jaPassou = !LocalDateTime.of(horario.getData(), horario.getHora())
                .isAfter(LocalDateTime.now(FUSO));
        if (jaPassou
                || agendamentoRepository.existsByHorarioIdAndStatusIn(horario.getId(), STATUS_ATIVOS)) {
            throw horarioIndisponivel();
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setFotografo(fotografo);
        agendamento.setServico(servico);
        agendamento.setHorario(horario);
        agendamento.setData(horario.getData());
        agendamento.setHora(horario.getHora());
        agendamento.setCodigo(gerarCodigo());
        agendamento.setClienteNome(req.nome().trim());
        agendamento.setClienteEmail(req.email().trim().toLowerCase());
        agendamento.setClienteTelefone(vazioParaNull(req.telefone()));
        agendamento.setObservacao(vazioParaNull(req.observacao()));

        try {
            // Flush imediato: se outro cliente pegou o horario neste instante,
            // o indice unico ux_agendamento_horario_ativo recusa aqui.
            agendamentoRepository.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException ex) {
            throw horarioIndisponivel();
        }
        return AgendamentoPublicoResponse.de(agendamento);
    }

    @Transactional(readOnly = true)
    public AgendamentoPublicoResponse consultar(String codigo) {
        return AgendamentoPublicoResponse.de(buscarPorCodigo(codigo));
    }

    @Transactional
    public AgendamentoPublicoResponse cancelar(String codigo) {
        Agendamento agendamento = buscarPorCodigo(codigo);
        if (!agendamento.getStatus().ativo()) {
            throw RegraNegocioException.conflito("TRANSICAO_INVALIDA",
                    "Este agendamento já foi encerrado e não pode ser cancelado.");
        }
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
        return AgendamentoPublicoResponse.de(agendamento);
    }

    /** Link inexistente e conta suspensa respondem o mesmo 404. */
    private Fotografo fotografoDoLink(String slug) {
        return fotografoRepository.findBySlug(slug)
                .filter(f -> f.getStatusAssinatura() != StatusAssinatura.SUSPENSA)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Página não encontrada."));
    }

    private Agendamento buscarPorCodigo(String codigo) {
        return agendamentoRepository.findByCodigo(codigo)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Agendamento não encontrado."));
    }

    private static RegraNegocioException horarioIndisponivel() {
        return RegraNegocioException.conflito("HORARIO_INDISPONIVEL",
                "Este horário não está mais disponível. Escolha outro.");
    }

    /** 24 caracteres aleatorios, seguros para usar em URL. */
    private String gerarCodigo() {
        byte[] bytes = new byte[18];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validarPeriodo(LocalDate de, LocalDate ate) {
        if (ate.isBefore(de)) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "VALIDACAO",
                    "A data final não pode ser anterior à data inicial.");
        }
        if (ChronoUnit.DAYS.between(de, ate) > PERIODO_MAXIMO_DIAS) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "VALIDACAO",
                    "O período pode ter no máximo um ano.");
        }
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
