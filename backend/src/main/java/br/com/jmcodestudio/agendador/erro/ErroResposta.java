package br.com.jmcodestudio.agendador.erro;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Corpo unico de erro da API. "campos" so aparece em erro de validacao. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(String codigo, String mensagem, Map<String, String> campos) {

    public ErroResposta(String codigo, String mensagem) {
        this(codigo, mensagem, null);
    }
}
