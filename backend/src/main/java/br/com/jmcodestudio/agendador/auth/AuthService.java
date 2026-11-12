package br.com.jmcodestudio.agendador.auth;

import br.com.jmcodestudio.agendador.domain.Fotografo;
import br.com.jmcodestudio.agendador.erro.RegraNegocioException;
import br.com.jmcodestudio.agendador.fotografo.FotografoRepository;
import br.com.jmcodestudio.agendador.fotografo.FotografoResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class AuthService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int DIAS_DE_TESTE = 30;

    private final FotografoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(FotografoRepository repository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse cadastrar(CadastroRequest req) {
        String email = normalizarEmail(req.email());
        if (repository.existsByEmail(email)) {
            throw RegraNegocioException.conflito("EMAIL_EM_USO", "Este e-mail já está cadastrado.");
        }
        if (repository.existsBySlug(req.slug())) {
            throw RegraNegocioException.conflito("SLUG_EM_USO", "Este link já está em uso. Escolha outro.");
        }

        Fotografo fotografo = new Fotografo();
        fotografo.setNome(req.nome().trim());
        fotografo.setEmail(email);
        fotografo.setSenhaHash(passwordEncoder.encode(req.senha()));
        fotografo.setSlug(req.slug());
        fotografo.setTesteAte(LocalDate.now(FUSO).plusDays(DIAS_DE_TESTE));
        repository.save(fotografo);

        return resposta(fotografo);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = normalizarEmail(req.email());
        // Lanca AuthenticationException (-> 401) se e-mail ou senha estiverem errados.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.senha()));

        Fotografo fotografo = repository.findByEmail(email).orElseThrow();
        return resposta(fotografo);
    }

    private AuthResponse resposta(Fotografo fotografo) {
        return new AuthResponse(tokenService.gerar(fotografo), FotografoResponse.de(fotografo));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
