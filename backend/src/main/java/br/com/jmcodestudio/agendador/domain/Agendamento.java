package br.com.jmcodestudio.agendador.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "agendamento")
@Getter
@Setter
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fotografo_id", nullable = false)
    private Fotografo fotografo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /** Fica null se o fotografo apagar o horario de um pedido ja encerrado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id")
    private HorarioDisponivel horario;

    /** Copia da data/hora do horario, preenchida ao criar e ao reagendar. */
    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 32, unique = true, updatable = false)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    @Column(name = "cliente_nome", nullable = false, length = 120)
    private String clienteNome;

    @Column(name = "cliente_email", nullable = false, length = 160)
    private String clienteEmail;

    @Column(name = "cliente_telefone", length = 20)
    private String clienteTelefone;

    @Column(length = 1000)
    private String observacao;

    @Column(length = 500)
    private String motivo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
