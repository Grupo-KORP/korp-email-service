package com.korp.email.infrastructure.messaging;

import com.korp.email.infrastructure.mail.EmailSenderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitEmailConsumer {

    private final EmailSenderService emailSenderService;

    public RabbitEmailConsumer(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @RabbitListener(queues = "${broker.queue.name}")
    public void receber(EmailQueueMessage mensagem) {
        emailSenderService.enviar(mensagem.mensagem(), mensagem.emailLogId());
    }
}
