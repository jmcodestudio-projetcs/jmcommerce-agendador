package br.com.jmcodestudio.agendador.conta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarContaRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o endereço do seu link.")
        @Size(min = 3, max = 60, message = "O link deve ter entre 3 e 60 caracteres.")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "Use apenas letras minúsculas, números e hífen.")
        String slug,

        @Pattern(regexp = "^$|^[0-9]{10,15}$",
                message = "Informe apenas números, com DDI e DDD. Ex.: 5511999990000.")
        String telefoneWhatsapp,

        @Size(max = 500, message = "A bio pode ter no máximo 500 caracteres.")
        String bio) {
}
