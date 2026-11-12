package br.com.jmcodestudio.agendador.agendamento;

import br.com.jmcodestudio.agendador.domain.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Usado para decidir se um servico pode ser apagado ou apenas desativado. */
    boolean existsByServicoId(Long servicoId);
}
