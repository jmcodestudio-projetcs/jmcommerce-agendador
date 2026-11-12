package br.com.jmcodestudio.agendador.publico;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Rotas abertas (sem token), usadas pelo cliente final. */
@RestController
@RequestMapping("/api/v1/publico")
public class PublicoController {

    private final PublicoService publicoService;

    public PublicoController(PublicoService publicoService) {
        this.publicoService = publicoService;
    }

    @GetMapping("/{slug}")
    public PerfilPublicoResponse perfil(@PathVariable String slug) {
        return publicoService.perfil(slug);
    }

    @GetMapping("/{slug}/horarios")
    public List<HorarioLivreResponse> horariosLivres(
            @PathVariable String slug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return publicoService.horariosLivres(slug, de, ate);
    }

    @PostMapping("/{slug}/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoPublicoResponse criarPedido(@PathVariable String slug,
                                                  @Valid @RequestBody PedidoRequest req) {
        return publicoService.criarPedido(slug, req);
    }

    @GetMapping("/agendamentos/{codigo}")
    public AgendamentoPublicoResponse consultar(@PathVariable String codigo) {
        return publicoService.consultar(codigo);
    }

    @PostMapping("/agendamentos/{codigo}/cancelar")
    public AgendamentoPublicoResponse cancelar(@PathVariable String codigo) {
        return publicoService.cancelar(codigo);
    }
}
