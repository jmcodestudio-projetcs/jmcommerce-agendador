package br.com.jmcodestudio.agendador.agendamento;

import br.com.jmcodestudio.agendador.domain.Agendamento;
import br.com.jmcodestudio.agendador.domain.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Codigo que o cliente recebe para acompanhar o pedido sem login. */
    Optional<Agendamento> findByCodigo(String codigo);

    /** Usado para decidir se um servico pode ser apagado ou apenas desativado. */
    boolean existsByServicoId(Long servicoId);

    /** Ha agendamento nesse horario com algum dos status informados? */
    boolean existsByHorarioIdAndStatusIn(Long horarioId, Collection<StatusAgendamento> status);

    /** Ids dos horarios ocupados (agendamento ativo) do fotografo em um periodo. */
    @Query("""
            select a.horario.id from Agendamento a
            where a.fotografo.id = :fotografoId
              and a.status in :status
              and a.data between :de and :ate
            """)
    Set<Long> idsDeHorariosOcupados(@Param("fotografoId") Long fotografoId,
                                    @Param("status") Collection<StatusAgendamento> status,
                                    @Param("de") LocalDate de,
                                    @Param("ate") LocalDate ate);

    /** Busca sempre junto com o dono: um fotografo nunca enxerga agendamento de outro. */
    Optional<Agendamento> findByIdAndFotografoId(Long id, Long fotografoId);

    /** Listagem do painel, ja trazendo servico e horario para evitar consultas extras. */
    @Query("""
            select a from Agendamento a
            join fetch a.servico
            left join fetch a.horario
            where a.fotografo.id = :fotografoId
              and a.status in :status
              and a.data between :de and :ate
            order by a.data, a.hora
            """)
    List<Agendamento> listarDoPainel(@Param("fotografoId") Long fotografoId,
                                     @Param("status") Collection<StatusAgendamento> status,
                                     @Param("de") LocalDate de,
                                     @Param("ate") LocalDate ate);
}
