package br.com.jmcodestudio.agendador.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fotografo")
@Getter
@Setter
public class Fotografo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false, length = 60, unique = true)
    private String slug;

    @Column(name = "telefone_whatsapp", length = 20)
    private String telefoneWhatsapp;

    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_assinatura", nullable = false, length = 20)
    private StatusAssinatura statusAssinatura = StatusAssinatura.TESTE;

    @Column(name = "teste_ate", nullable = false)
    private LocalDate testeAte;

    @Column(name = "token_redefinicao", length = 64)
    private String tokenRedefinicao;

    @Column(name = "token_redefinicao_expira_em")
    private OffsetDateTime tokenRedefinicaoExpiraEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();
}
