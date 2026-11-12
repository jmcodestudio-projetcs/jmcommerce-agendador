package br.com.jmcodestudio.agendador.servico;

import br.com.jmcodestudio.agendador.domain.Servico;

import java.math.BigDecimal;

/** Objeto "servico" do contrato. */
public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        Integer duracaoMinutos,
        BigDecimal preco,
        boolean exibirPreco,
        boolean ativo) {

    public static ServicoResponse de(Servico s) {
        return new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(),
                s.getDuracaoMinutos(), s.getPreco(), s.isExibirPreco(), s.isAtivo());
    }
}
