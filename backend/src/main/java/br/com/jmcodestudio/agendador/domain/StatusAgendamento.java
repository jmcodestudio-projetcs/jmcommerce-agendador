package br.com.jmcodestudio.agendador.domain;

public enum StatusAgendamento {
    PENDENTE,
    APROVADO,
    RECUSADO,
    CANCELADO;

    /** Ativo = ocupa o horario. */
    public boolean ativo() {
        return this == PENDENTE || this == APROVADO;
    }
}
