package br.com.jmcodestudio.agendador.erro;

import org.springframework.http.HttpStatus;

/** Erro de regra de negocio: carrega o status HTTP e o codigo do contrato. */
public class RegraNegocioException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    public RegraNegocioException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public static RegraNegocioException conflito(String codigo, String mensagem) {
        return new RegraNegocioException(HttpStatus.CONFLICT, codigo, mensagem);
    }

    public static RegraNegocioException naoEncontrado(String mensagem) {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", mensagem);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
