package br.com.jmcodestudio.agendador.agenda;

import br.com.jmcodestudio.agendador.agendamento.AgendamentoRepository;
import br.com.jmcodestudio.agendador.domain.Fotografo;
import br.com.jmcodestudio.agendador.domain.HorarioDisponivel;
import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import br.com.jmcodestudio.agendador.erro.RegraNegocioException;
import br.com.jmcodestudio.agendador.fotografo.FotografoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AgendaService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int PERIODO_MAXIMO_DIAS = 366;
    private static final List<StatusAgendamento> STATUS_ATIVOS =
            List.of(StatusAgendamento.PENDENTE, StatusAgendamento.APROVADO);

    private final HorarioRepository horarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final FotografoRepository fotografoRepository;

    public AgendaService(HorarioRepository horarioRepository,
                         AgendamentoRepository agendamentoRepository,
                         FotografoRepository fotografoRepository) {
        this.horarioRepository = horarioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.fotografoRepository = fotografoRepository;
    }

    @Transactional(readOnly = true)
    public List<HorarioResponse> listar(Long fotografoId, LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        Set<Long> ocupados =
                agendamentoRepository.idsDeHorariosOcupados(fotografoId, STATUS_ATIVOS, de, ate);
        return horarioRepository
                .findByFotografoIdAndDataBetweenOrderByDataAscHoraAsc(fotografoId, de, ate).stream()
                .map(h -> HorarioResponse.de(h, ocupados.contains(h.getId())))
                .toList();
    }

    /**
     * Cria uma linha para cada combinacao de dia e hora do periodo.
     * Horarios que ja existem e dias que ja passaram sao ignorados, sem erro.
     */
    @Transactional
    public AbrirHorariosResponse abrir(Long fotografoId, AbrirHorariosRequest req) {
        validarPeriodo(req.dataInicio(), req.dataFim());

        Set<Integer> diasSemana = (req.diasSemana() == null || req.diasSemana().isEmpty())
                ? Set.of(1, 2, 3, 4, 5, 6, 7)
                : new HashSet<>(req.diasSemana());

        // Sem segundos, e sem horas repetidas no mesmo pedido.
        Set<LocalTime> horas = new HashSet<>();
        for (LocalTime hora : req.horas()) {
            horas.add(hora.truncatedTo(ChronoUnit.MINUTES));
        }

        Set<LocalDateTime> jaExistem = new HashSet<>();
        for (HorarioDisponivel h : horarioRepository
                .findByFotografoIdAndDataBetweenOrderByDataAscHoraAsc(
                        fotografoId, req.dataInicio(), req.dataFim())) {
            jaExistem.add(LocalDateTime.of(h.getData(), h.getHora()));
        }

        Fotografo fotografo = fotografoRepository.getReferenceById(fotografoId);
        LocalDate hoje = LocalDate.now(FUSO);
        List<HorarioDisponivel> novos = new ArrayList<>();

        for (LocalDate dia = req.dataInicio(); !dia.isAfter(req.dataFim()); dia = dia.plusDays(1)) {
            if (dia.isBefore(hoje) || !diasSemana.contains(dia.getDayOfWeek().getValue())) {
                continue;
            }
            for (LocalTime hora : horas) {
                if (jaExistem.contains(LocalDateTime.of(dia, hora))) {
                    continue;
                }
                HorarioDisponivel horario = new HorarioDisponivel();
                horario.setFotografo(fotografo);
                horario.setData(dia);
                horario.setHora(hora);
                novos.add(horario);
            }
        }

        horarioRepository.saveAll(novos);
        return new AbrirHorariosResponse(novos.size());
    }

    @Transactional
    public void remover(Long fotografoId, Long id) {
        HorarioDisponivel horario = horarioRepository.findByIdAndFotografoId(id, fotografoId)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Horário não encontrado."));

        if (agendamentoRepository.existsByHorarioIdAndStatusIn(horario.getId(), STATUS_ATIVOS)) {
            throw RegraNegocioException.conflito("HORARIO_COM_AGENDAMENTO",
                    "Este horário tem um agendamento pendente ou aprovado. "
                            + "Cancele ou reagende o agendamento antes de remover.");
        }
        // Agendamentos ja encerrados desse horario ficam com horario_id nulo (ON DELETE SET NULL).
        horarioRepository.delete(horario);
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
}
