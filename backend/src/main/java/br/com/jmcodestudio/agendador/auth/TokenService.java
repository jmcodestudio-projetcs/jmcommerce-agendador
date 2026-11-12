package br.com.jmcodestudio.agendador.auth;

import br.com.jmcodestudio.agendador.domain.Fotografo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Gera o JWT. O "subject" do token e o id do fotografo. */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiracaoDias;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.jwt.expiracao-dias}") long expiracaoDias) {
        this.jwtEncoder = jwtEncoder;
        this.expiracaoDias = expiracaoDias;
    }

    public String gerar(Fotografo fotografo) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(fotografo.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoDias, ChronoUnit.DAYS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
