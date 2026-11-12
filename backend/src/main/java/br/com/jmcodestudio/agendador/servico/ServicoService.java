package br.com.jmcodestudio.agendador.servico;

import br.com.jmcodestudio.agendador.agendamento.AgendamentoRepository;
import br.com.jmcodestudio.agendador.domain.Servico;
import br.com.jmcodestudio.agendador.erro.RegraNegocioException;
import br.com.jmcodestudio.agendador.fotografo.FotografoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final FotografoRepository fotografoRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ServicoService(ServicoRepository servicoRepository,
                          FotografoRepository fotografoRepository,
                          AgendamentoRepository agendamentoRepository) {
        this.servicoRepository = servicoRepository;
        this.fotografoRepository = fotografoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar(Long fotografoId) {
        return servicoRepository.findByFotografoIdOrderByNomeAsc(fotografoId).stream()
                .map(ServicoResponse::de)
                .toList();
    }

    @Transactional
    public ServicoResponse criar(Long fotografoId, ServicoRequest req) {
        Servico servico = new Servico();
        // getReferenceById: liga o servico ao fotografo sem fazer SELECT.
        servico.setFotografo(fotografoRepository.getReferenceById(fotografoId));
        aplicar(servico, req);
        return ServicoResponse.de(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizar(Long fotografoId, Long id, ServicoRequest req) {
        Servico servico = buscar(fotografoId, id);
        aplicar(servico, req);
        return ServicoResponse.de(servicoRepository.save(servico));
    }

    /** Apaga o servico; se ja houver agendamento ligado a ele, apenas desativa. */
    @Transactional
    public void excluir(Long fotografoId, Long id) {
        Servico servico = buscar(fotografoId, id);
        if (agendamentoRepository.existsByServicoId(servico.getId())) {
            servico.setAtivo(false);
            servicoRepository.save(servico);
        } else {
            servicoRepository.delete(servico);
        }
    }

    private Servico buscar(Long fotografoId, Long id) {
        return servicoRepository.findByIdAndFotografoId(id, fotografoId)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Serviço não encontrado."));
    }

    private void aplicar(Servico servico, ServicoRequest req) {
        servico.setNome(req.nome().trim());
        servico.setDescricao(vazioParaNull(req.descricao()));
        servico.setDuracaoMinutos(req.duracaoMinutos());
        servico.setPreco(req.preco());
        servico.setExibirPreco(Boolean.TRUE.equals(req.exibirPreco()));
        servico.setAtivo(req.ativo() == null || req.ativo());
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
