package com.korp.email.domain.model;

import java.time.LocalDateTime;

/**
 * Registro de um envio de e-mail: o que foi pedido, para quem, e em
 * que estado está (PENDENTE -> ENVIADO / FALHOU).
 * <p>
 * É criado como PENDENTE de forma síncrona (rápido, é isso que
 * permite o controller responder 202 Accepted na hora), e depois
 * atualizado pela infraestrutura quando o envio assíncrono termina.
 */
public class EmailLog {

    private Long id;
    private final EmailType tipo;
    private final String destinatario;
    private EmailStatus status;
    private String mensagemErro;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    private EmailLog(Long id, EmailType tipo, String destinatario, EmailStatus status,
                      String mensagemErro, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.status = status;
        this.mensagemErro = mensagemErro;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** Usado pelos usecases para criar um novo registro, ainda sem id (definido na persistência). */
    public static EmailLog novoPendente(EmailType tipo, String destinatario) {
        LocalDateTime agora = LocalDateTime.now();
        return new EmailLog(null, tipo, destinatario, EmailStatus.PENDENTE, null, agora, agora);
    }

    /** Usado pela camada de persistência para reconstruir um registro já existente. */
    public static EmailLog reconstruir(Long id, EmailType tipo, String destinatario, EmailStatus status,
                                        String mensagemErro, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        return new EmailLog(id, tipo, destinatario, status, mensagemErro, criadoEm, atualizadoEm);
    }

    public void marcarComoEnviado() {
        this.status = EmailStatus.ENVIADO;
        this.mensagemErro = null;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void marcarComoFalha(String motivo) {
        this.status = EmailStatus.FALHOU;
        this.mensagemErro = motivo;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void definirId(Long id) {
        this.id = id;
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
