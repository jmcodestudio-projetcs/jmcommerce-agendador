package br.com.jmcodestudio.agendador.agendamento;

import jakarta.validation.constraints.Size;

/** Corpo opcional de recusar e cancelar. */
public record MotivoRequest(
        @Size(max = 500, message = "O motivo pode ter no máximo 500 caracteres.")
        String motivo) {
}
