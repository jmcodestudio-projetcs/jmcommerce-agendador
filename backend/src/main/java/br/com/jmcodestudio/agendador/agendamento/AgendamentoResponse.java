package br.com.jmcodestudio.agendador.agendamento;

import br.com.jmcodestudio.agendador.domain.Agendamento;
import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/** Objeto "agendamento" do contrato, como o fotografo ve no painel. */
public record AgendamentoResponse(
        Long id,
        String codigo,
        StatusAgendamento status,
        ServicoResumo servico,
        HorarioResumo horario,
        ClienteResumo cliente,
        String observacao,
        String motivo,
        OffsetDateTime criadoEm) {

    public record ServicoResumo(Long id, String nome) {
    }

    /** "id" fica null se o horario foi apagado depois de o agendamento ser encerrado. */
    public record HorarioResumo(Long id, LocalDate data,
                                @JsonFormat(pattern = "HH:mm") LocalTime hora) {
    }

    public record ClienteResumo(String nome, String email, String telefone) {
    }

    /** Chamar dentro de transacao: le o servico (relacao LAZY). */
    public static AgendamentoResponse de(Agendamento a) {
        Long horarioId = a.getHorario() == null ? null : a.getHorario().getId();
        return new AgendamentoResponse(
                a.getId(), a.getCodigo(), a.getStatus(),
                new ServicoResumo(a.getServico().getId(), a.getServico().getNome()),
                new HorarioResumo(horarioId, a.getData(), a.getHora()),
                new ClienteResumo(a.getClienteNome(), a.getClienteEmail(), a.getClienteTelefone()),
                a.getObservacao(), a.getMotivo(), a.getCriadoEm());
    }
}
