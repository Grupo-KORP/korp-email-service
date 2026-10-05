package com.korp.email.domain.gateway;

import com.korp.email.domain.model.EmailMessage;

/**
 * Porta responsável por efetivamente disparar o envio do e-mail.
 * <p>
 * O ponto importante: essa porta não espera o envio SMTP terminar.
 * A implementação pode confirmar uma publicação em fila antes de
 * retornar; o consumidor atualiza o {@code EmailLog} após o envio.
 * <p>
 * Isso mantém os usecases síncronos e simples: eles só criam o log
 * como PENDENTE e disparam o despacho, sem se importar com a
 * mecânica assíncrona.
 */
public interface EmailDispatcherGateway {

    void despachar(EmailMessage mensagem, Long emailLogId);
}
