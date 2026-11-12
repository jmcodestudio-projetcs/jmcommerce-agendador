package br.com.jmcodestudio.agendador.agenda;

import br.com.jmcodestudio.agendador.domain.HorarioDisponivel;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** Objeto "horario" do contrato. */
public record HorarioResponse(
        Long id,
        LocalDate data,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        boolean ocupado) {

    public static HorarioResponse de(HorarioDisponivel h, boolean ocupado) {
        return new HorarioResponse(h.getId(), h.getData(), h.getHora(), ocupado);
    }
}
