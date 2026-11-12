package br.com.jmcodestudio.agendador.agenda;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/** Agenda do fotografo logado: horarios que ele abre manualmente. */
@RestController
@RequestMapping("/api/v1/painel/horarios")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping
    public List<HorarioResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return agendaService.listar(fotografoId(jwt), de, ate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbrirHorariosResponse abrir(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody AbrirHorariosRequest req) {
        return agendaService.abrir(fotografoId(jwt), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        agendaService.remover(fotografoId(jwt), id);
    }

    private Long fotografoId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
