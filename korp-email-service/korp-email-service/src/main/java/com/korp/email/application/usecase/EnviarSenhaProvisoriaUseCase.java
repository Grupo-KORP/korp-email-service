package com.korp.email.application.usecase;

import com.korp.email.application.template.EmailTemplates;
import com.korp.email.application.usecase.command.EnviarSenhaProvisoriaCommand;
import com.korp.email.domain.gateway.EmailDispatcherGateway;
import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.domain.model.EmailMessage;
import com.korp.email.domain.model.EmailType;

public class EnviarSenhaProvisoriaUseCase {

    private final EmailLogRepository emailLogRepository;
    private final EmailDispatcherGateway emailDispatcherGateway;

    public EnviarSenhaProvisoriaUseCase(EmailLogRepository emailLogRepository,
                                         EmailDispatcherGateway emailDispatcherGateway) {
        this.emailLogRepository = emailLogRepository;
        this.emailDispatcherGateway = emailDispatcherGateway;
    }

    public Long executar(EnviarSenhaProvisoriaCommand command) {
        String corpoHtml = EmailTemplates.senhaProvisoria(command.nome(), command.senhaProvisoria());
        String assunto = "Aqui está sua senha de acesso para o Sistema de Comissões - TND";

        EmailMessage mensagem = new EmailMessage(command.email(), assunto, corpoHtml, null);

        EmailLog log = EmailLog.novoPendente(EmailType.SENHA_PROVISORIA, command.email());
        log = emailLogRepository.salvar(log);

        emailDispatcherGateway.despachar(mensagem, log.getId());

        return log.getId();
    }
}
