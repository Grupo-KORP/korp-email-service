package com.korp.email.infrastructure.messaging;

import com.korp.email.domain.model.EmailMessage;

/** Mensagem interna publicada no RabbitMQ para processamento de um e-mail. */
public record EmailQueueMessage(Long emailLogId, EmailMessage mensagem) {
}
