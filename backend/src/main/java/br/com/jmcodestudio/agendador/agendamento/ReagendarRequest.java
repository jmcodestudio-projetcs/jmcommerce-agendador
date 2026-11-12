package br.com.jmcodestudio.agendador.agendamento;

import jakarta.validation.constraints.NotNull;

public record ReagendarRequest(
        @NotNull(message = "Escolha o novo horário.")
        Long horarioId) {
}
