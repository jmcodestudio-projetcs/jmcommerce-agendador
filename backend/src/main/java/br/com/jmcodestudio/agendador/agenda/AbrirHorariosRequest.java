package br.com.jmcodestudio.agendador.agenda;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Abertura de agenda por periodo. diasSemana: 1 = segunda ... 7 = domingo. */
public record AbrirHorariosRequest(
        @NotNull(message = "Informe a data inicial.")
        LocalDate dataInicio,

        @NotNull(message = "Informe a data final.")
        LocalDate dataFim,

        // Opcional: sem valor, vale para todos os dias da semana.
        List<@NotNull @Min(value = 1, message = "Dia da semana inválido.")
             @Max(value = 7, message = "Dia da semana inválido.") Integer> diasSemana,

        @NotEmpty(message = "Informe pelo menos um horário.")
        @Size(max = 24, message = "Informe no máximo 24 horários por dia.")
        List<@NotNull(message = "Horário inválido.") LocalTime> horas) {
}
