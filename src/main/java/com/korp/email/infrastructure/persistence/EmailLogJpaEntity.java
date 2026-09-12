package com.korp.email.infrastructure.persistence;

import com.korp.email.domain.model.EmailStatus;
import com.korp.email.domain.model.EmailType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
public class EmailLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmailType tipo;

    @Column(nullable = false)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;

    @Column(length = 1000)
    private String mensagemErro;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    protected EmailLogJpaEntity() {

    }

    public EmailLogJpaEntity(Long id, EmailType tipo, String destinatario, EmailStatus status,
                              String mensagemErro, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.status = status;
        this.mensagemErro = mensagemErro;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() {
        return id;
    }

    public EmailType getTipo() {
        return tipo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
