package br.com.jmcodestudio.agendador.servico;

import br.com.jmcodestudio.agendador.domain.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findByFotografoIdOrderByNomeAsc(Long fotografoId);

    /** Busca sempre junto com o dono: um fotografo nunca enxerga servico de outro. */
    Optional<Servico> findByIdAndFotografoId(Long id, Long fotografoId);
}
