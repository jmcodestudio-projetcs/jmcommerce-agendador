package br.com.jmcodestudio.agendador.conta;

import br.com.jmcodestudio.agendador.domain.Fotografo;
import br.com.jmcodestudio.agendador.erro.RegraNegocioException;
import br.com.jmcodestudio.agendador.fotografo.FotografoRepository;
import br.com.jmcodestudio.agendador.fotografo.FotografoResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Conta do fotografo logado. O fotografo vem sempre do token, nunca da URL. */
@RestController
@RequestMapping("/api/v1/painel/conta")
public class ContaController {

    private final FotografoRepository repository;

    public ContaController(FotografoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public FotografoResponse ver(@AuthenticationPrincipal Jwt jwt) {
        return FotografoResponse.de(fotografoLogado(jwt));
    }

    @PutMapping
    public FotografoResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody AtualizarContaRequest req) {
        Fotografo fotografo = fotografoLogado(jwt);

        boolean mudouSlug = !fotografo.getSlug().equals(req.slug());
        if (mudouSlug && repository.existsBySlug(req.slug())) {
            throw RegraNegocioException.conflito("SLUG_EM_USO", "Este link já está em uso. Escolha outro.");
        }

        fotografo.setNome(req.nome().trim());
        fotografo.setSlug(req.slug());
        fotografo.setTelefoneWhatsapp(vazioParaNull(req.telefoneWhatsapp()));
        fotografo.setBio(vazioParaNull(req.bio()));
        return FotografoResponse.de(repository.save(fotografo));
    }

    private Fotografo fotografoLogado(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        return repository.findById(id)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Conta não encontrada."));
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
