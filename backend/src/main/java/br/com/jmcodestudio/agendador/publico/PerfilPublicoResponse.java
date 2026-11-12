package br.com.jmcodestudio.agendador.publico;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

/** Pagina publica do fotografo: dados de apresentacao e servicos ativos. */
public record PerfilPublicoResponse(
        String nome,
        String bio,
        String telefoneWhatsapp,
        List<ServicoPublico> servicos) {

    /** "preco" so e enviado quando o fotografo marcou exibirPreco. */
    public record ServicoPublico(
            Long id,
            String nome,
            String descricao,
            Integer duracaoMinutos,
            @JsonInclude(JsonInclude.Include.NON_NULL) BigDecimal preco) {
    }
}
