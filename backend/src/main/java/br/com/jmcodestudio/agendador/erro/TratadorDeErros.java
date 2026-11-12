package br.com.jmcodestudio.agendador.erro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte toda excecao no corpo padrao de erro, com o status HTTP correto.
 * Estende o tratador do Spring para que os erros do proprio MVC
 * (JSON invalido, metodo nao permitido, rota inexistente...) saiam no mesmo formato.
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraDeNegocio(RegraNegocioException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErroResposta(ex.getCodigo(), ex.getMessage()));
    }

    /** Login com e-mail ou senha errados (lancado pelo AuthenticationManager). */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> naoAutenticado(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErroResposta("NAO_AUTENTICADO", "E-mail ou senha incorretos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroResposta("ACESSO_NEGADO", "Você não tem permissão para esta ação."));
    }

    /** Ultima barreira: uma constraint do banco barrou a operacao. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> conflitoNoBanco(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta("CONFLITO", "A operação conflita com dados já existentes."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> erroInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResposta("ERRO_INTERNO", "Erro inesperado. Tente novamente."));
    }

    /** Validacao de campos (@Valid): 400 com a mensagem de cada campo. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErroResposta("VALIDACAO", "Verifique os campos informados.", campos));
    }

    /** Demais erros do Spring MVC: mantem o status dele e troca o corpo pelo nosso. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Erro interno", ex);
        }
        return ResponseEntity.status(statusCode).headers(headers).body(erroPara(statusCode.value()));
    }

    private ErroResposta erroPara(int status) {
        return switch (status) {
            case 400 -> new ErroResposta("REQUISICAO_INVALIDA", "Requisição inválida.");
            case 401 -> new ErroResposta("NAO_AUTENTICADO", "Faça login para continuar.");
            case 403 -> new ErroResposta("ACESSO_NEGADO", "Você não tem permissão para esta ação.");
            case 404 -> new ErroResposta("NAO_ENCONTRADO", "Recurso não encontrado.");
            case 405 -> new ErroResposta("METODO_NAO_PERMITIDO", "Método não permitido para esta rota.");
            case 415 -> new ErroResposta("TIPO_NAO_SUPORTADO", "Envie o corpo como application/json.");
            default -> status >= 500
                    ? new ErroResposta("ERRO_INTERNO", "Erro inesperado. Tente novamente.")
                    : new ErroResposta("REQUISICAO_INVALIDA", "Requisição inválida.");
        };
    }
}
