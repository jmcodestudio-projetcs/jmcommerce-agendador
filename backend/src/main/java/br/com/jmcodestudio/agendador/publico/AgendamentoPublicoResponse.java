package br.com.jmcodestudio.agendador.publico;

import br.com.jmcodestudio.agendador.domain.Agendamento;
import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** Objeto "agendamentoPublico" do contrato: o que o cliente ve do seu pedido. */
public record AgendamentoPublicoResponse(
        String codigo,
        StatusAgendamento status,
        String servico,
        LocalDate data,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        String fotografo,
        String telefoneWhatsapp) {

    /** Chamar dentro de transacao: le servico e fotografo (relacoes LAZY). */
    public static AgendamentoPublicoResponse de(Agendamento a) {
        return new AgendamentoPublicoResponse(a.getCodigo(), a.getStatus(),
                a.getServico().getNome(), a.getData(), a.getHora(),
                a.getFotografo().getNome(), a.getFotografo().getTelefoneWhatsapp());
    }
}
