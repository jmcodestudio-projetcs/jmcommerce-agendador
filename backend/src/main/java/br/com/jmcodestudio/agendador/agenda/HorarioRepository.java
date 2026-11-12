package br.com.jmcodestudio.agendador.agenda;

import br.com.jmcodestudio.agendador.domain.HorarioDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HorarioRepository extends JpaRepository<HorarioDisponivel, Long> {

    List<HorarioDisponivel> findByFotografoIdAndDataBetweenOrderByDataAscHoraAsc(
            Long fotografoId, LocalDate de, LocalDate ate);

    Optional<HorarioDisponivel> findByIdAndFotografoId(Long id, Long fotografoId);
}
