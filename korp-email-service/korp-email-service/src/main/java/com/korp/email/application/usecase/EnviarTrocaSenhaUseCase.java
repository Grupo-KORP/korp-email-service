package com.korp.email.application.usecase;

import com.korp.email.application.template.EmailTemplates;
import com.korp.email.application.usecase.command.EnviarTrocaSenhaCommand;
import com.korp.email.domain.gateway.EmailDispatcherGateway;
import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.domain.model.EmailMessage;
import com.korp.email.domain.model.EmailType;

public class EnviarTrocaSenhaUseCase {

    private final EmailLogRepository emailLogRepository;
    private final EmailDispatcherGateway emailDispatcherGateway;

    public EnviarTrocaSenhaUseCase(EmailLogRepository emailLogRepository,
                                    EmailDispatcherGateway emailDispatcherGateway) {
        this.emailLogRepository = emailLogRepository;
        this.emailDispatcherGateway = emailDispatcherGateway;
    }

    public Long executar(EnviarTrocaSenhaCommand command) {
        String corpoHtml = EmailTemplates.trocaSenha(command.nome(), command.urlRedefinicao());
        String assunto = "Aqui está seu link para Alterar a senha";

        EmailMessage mensagem = new EmailMessage(command.email(), assunto, corpoHtml, null);

        EmailLog log = EmailLog.novoPendente(EmailType.TROCA_SENHA, command.email());
        log = emailLogRepository.salvar(log);

        emailDispatcherGateway.despachar(mensagem, log.getId());

        return log.getId();
    }
}
