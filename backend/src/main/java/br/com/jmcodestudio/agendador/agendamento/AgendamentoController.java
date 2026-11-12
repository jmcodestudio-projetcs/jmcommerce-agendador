package br.com.jmcodestudio.agendador.agendamento;

import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Painel de agendamentos do fotografo logado. Cada acao tem o seu POST. */
@RestController
@RequestMapping("/api/v1/painel/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public List<AgendamentoResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return agendamentoService.listar(fotografoId(jwt), status, de, ate);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse ver(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return agendamentoService.ver(fotografoId(jwt), id);
    }

    @PostMapping("/{id}/aprovar")
    public AgendamentoResponse aprovar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return agendamentoService.aprovar(fotografoId(jwt), id);
    }

    @PostMapping("/{id}/recusar")
    public AgendamentoResponse recusar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                       @Valid @RequestBody(required = false) MotivoRequest req) {
        return agendamentoService.recusar(fotografoId(jwt), id, req == null ? null : req.motivo());
    }

    @PostMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                        @Valid @RequestBody(required = false) MotivoRequest req) {
        return agendamentoService.cancelar(fotografoId(jwt), id, req == null ? null : req.motivo());
    }

    @PostMapping("/{id}/reagendar")
    public AgendamentoResponse reagendar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                         @Valid @RequestBody ReagendarRequest req) {
        return agendamentoService.reagendar(fotografoId(jwt), id, req.horarioId());
    }

    private Long fotografoId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
