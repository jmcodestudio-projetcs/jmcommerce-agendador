package br.com.jmcodestudio.agendador.publico;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pedido de agendamento feito pelo cliente, que nao tem conta. */
public record PedidoRequest(
        @NotNull(message = "Escolha um serviço.")
        Long servicoId,

        @NotNull(message = "Escolha um horário.")
        Long horarioId,

        @NotBlank(message = "Informe seu nome.")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 160, message = "O e-mail pode ter no máximo 160 caracteres.")
        String email,

        @Size(max = 20, message = "O telefone pode ter no máximo 20 caracteres.")
        String telefone,

        @Size(max = 1000, message = "A observação pode ter no máximo 1000 caracteres.")
        String observacao) {
}
