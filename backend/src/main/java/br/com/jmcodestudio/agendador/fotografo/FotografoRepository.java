package br.com.jmcodestudio.agendador.fotografo;

import br.com.jmcodestudio.agendador.domain.Fotografo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FotografoRepository extends JpaRepository<Fotografo, Long> {

    Optional<Fotografo> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsBySlug(String slug);
}
