package br.com.jmcodestudio.agendador.servico;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Servicos do fotografo logado. O fotografo vem sempre do token. */
@RestController
@RequestMapping("/api/v1/painel/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    public List<ServicoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return servicoService.listar(fotografoId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse criar(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody ServicoRequest req) {
        return servicoService.criar(fotografoId(jwt), req);
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody ServicoRequest req) {
        return servicoService.atualizar(fotografoId(jwt), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        servicoService.excluir(fotografoId(jwt), id);
    }

    private Long fotografoId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
