package br.com.jmcodestudio.agendador.servico;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank(message = "Informe o nome do serviço.")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição pode ter no máximo 500 caracteres.")
        String descricao,

        @NotNull(message = "Informe a duração em minutos.")
        @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto.")
        @Max(value = 1440, message = "A duração pode ser de no máximo 1440 minutos.")
        Integer duracaoMinutos,

        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "Preço inválido.")
        BigDecimal preco,

        // Opcionais: sem valor, o serviço nasce ativo e sem exibir o preço.
        Boolean exibirPreco,
        Boolean ativo) {
}
