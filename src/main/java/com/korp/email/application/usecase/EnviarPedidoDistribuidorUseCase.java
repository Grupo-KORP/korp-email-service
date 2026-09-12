package com.korp.email.application.usecase;

import com.korp.email.application.template.EmailTemplates;
import com.korp.email.application.usecase.command.EnviarPedidoDistribuidorCommand;
import com.korp.email.domain.gateway.EmailDispatcherGateway;
import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.Attachment;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.domain.model.EmailMessage;
import com.korp.email.domain.model.EmailType;

public class EnviarPedidoDistribuidorUseCase {

    private final EmailLogRepository emailLogRepository;
    private final EmailDispatcherGateway emailDispatcherGateway;

    public EnviarPedidoDistribuidorUseCase(EmailLogRepository emailLogRepository,
                                            EmailDispatcherGateway emailDispatcherGateway) {
        this.emailLogRepository = emailLogRepository;
        this.emailDispatcherGateway = emailDispatcherGateway;
    }

    public Long executar(EnviarPedidoDistribuidorCommand command) {
        String corpoHtml = EmailTemplates.pedidoDistribuidor(
                command.nomeDistribuidor(), command.codigoPedido(), command.nomeCliente());
        String assunto = "Novo Pedido Registrado - Código: " + command.codigoPedido();

        Attachment anexo = new Attachment(
                "pedido-" + command.codigoPedido() + ".pdf",
                command.pdfBase64(),
                "application/pdf"
        );

        EmailMessage mensagem = new EmailMessage(command.emailDistribuidor(), assunto, corpoHtml, anexo);

        EmailLog log = EmailLog.novoPendente(EmailType.PEDIDO_DISTRIBUIDOR, command.emailDistribuidor());
        log = emailLogRepository.salvar(log);

        emailDispatcherGateway.despachar(mensagem, log.getId());

        return log.getId();
    }
}
