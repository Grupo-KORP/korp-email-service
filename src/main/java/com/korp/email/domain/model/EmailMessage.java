package com.korp.email.domain.model;

/**
 * Representa um e-mail já pronto para ser enviado: destinatário,
 * assunto, corpo em HTML e um anexo opcional.
 * <p>
 * É montado pelos usecases (camada de aplicação) e consumido pelo
 * {@link com.korp.email.domain.gateway.EmailDispatcherGateway} na
 * infraestrutura.
 */
public record EmailMessage(
        String destinatario,
        String assunto,
        String corpoHtml,
        Attachment anexo
) {

    public boolean temAnexo() {
        return anexo != null;
    }
}
