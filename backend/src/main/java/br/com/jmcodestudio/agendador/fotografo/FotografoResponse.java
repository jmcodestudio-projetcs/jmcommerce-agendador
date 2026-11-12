package br.com.jmcodestudio.agendador.fotografo;

import br.com.jmcodestudio.agendador.domain.Fotografo;
import br.com.jmcodestudio.agendador.domain.StatusAssinatura;

import java.time.LocalDate;

/** Objeto "fotografo" do contrato. Nunca expoe o hash da senha. */
public record FotografoResponse(
        Long id,
        String nome,
        String email,
        String slug,
        String telefoneWhatsapp,
        String bio,
        StatusAssinatura statusAssinatura,
        LocalDate testeAte) {

    public static FotografoResponse de(Fotografo f) {
        return new FotografoResponse(f.getId(), f.getNome(), f.getEmail(), f.getSlug(),
                f.getTelefoneWhatsapp(), f.getBio(), f.getStatusAssinatura(), f.getTesteAte());
    }
}
