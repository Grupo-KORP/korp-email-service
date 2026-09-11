package com.korp.email.infrastructure.web.dto;

public record EmailAceitoResponse(
        Long emailLogId,
        String status
) {
    public static EmailAceitoResponse pendente(Long emailLogId) {
        return new EmailAceitoResponse(emailLogId, "PENDENTE");
    }
}
