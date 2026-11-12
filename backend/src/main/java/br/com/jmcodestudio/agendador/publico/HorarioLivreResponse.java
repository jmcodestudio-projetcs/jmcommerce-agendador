package br.com.jmcodestudio.agendador.publico;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioLivreResponse(
        Long id,
        LocalDate data,
        @JsonFormat(pattern = "HH:mm") LocalTime hora) {
}
