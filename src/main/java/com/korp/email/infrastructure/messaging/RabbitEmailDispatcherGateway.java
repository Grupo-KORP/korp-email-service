package com.korp.email.infrastructure.messaging;

import com.korp.email.domain.gateway.EmailDispatcherGateway;
import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.domain.model.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Publica e-mails na fila e só retorna depois da confirmação do broker. */
@Component
public class RabbitEmailDispatcherGateway implements EmailDispatcherGateway {

    private static final Logger log = LoggerFactory.getLogger(RabbitEmailDispatcherGateway.class);
    private static final String ERRO_PUBLICACAO = "Falha ao publicar e-mail na fila RabbitMQ.";

    private final RabbitTemplate rabbitTemplate;
    private final EmailLogRepository emailLogRepository;
    private final String exchangeName;
    private final long confirmTimeoutMs;

    public RabbitEmailDispatcherGateway(
            RabbitTemplate rabbitTemplate,
            EmailLogRepository emailLogRepository,
            @Value("${broker.exchange.name}") String exchangeName,
            @Value("${broker.publisher-confirm-timeout-ms:5000}") long confirmTimeoutMs) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailLogRepository = emailLogRepository;
        this.exchangeName = exchangeName;
        this.confirmTimeoutMs = confirmTimeoutMs;
    }

    @Override
    public void despachar(EmailMessage mensagem, Long emailLogId) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        try {
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    "",
                    new EmailQueueMessage(emailLogId, mensagem),
                    rabbitMessage -> {
                        rabbitMessage.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return rabbitMessage;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(confirmTimeoutMs, TimeUnit.MILLISECONDS);

            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ recusou a publicação da mensagem.");
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ não encontrou fila vinculada ao exchange de e-mail.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            falhaAoPublicar(emailLogId, e);
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            falhaAoPublicar(emailLogId, e);
        }
    }

    private void falhaAoPublicar(Long emailLogId, Exception cause) {
        log.error("Falha ao publicar o envio de e-mail (logId={}) no RabbitMQ", emailLogId, cause);
        try {
            emailLogRepository.buscarPorId(emailLogId).ifPresentOrElse(emailLog -> {
                emailLog.marcarComoFalha(ERRO_PUBLICACAO);
                emailLogRepository.salvar(emailLog);
            }, () -> log.warn("EmailLog {} não encontrado após falha na publicação", emailLogId));
        } catch (RuntimeException persistenceException) {
            cause.addSuppressed(persistenceException);
            log.error("Não foi possível atualizar o EmailLog após falha de publicação (logId={})", emailLogId);
        }

        throw new EmailQueueUnavailableException("Não foi possível enfileirar o e-mail.", cause);
    }
}
