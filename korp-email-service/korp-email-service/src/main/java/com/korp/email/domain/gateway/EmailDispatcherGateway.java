package com.korp.email.domain.gateway;

import com.korp.email.domain.model.EmailMessage;

/**
 * Porta responsável por efetivamente disparar o envio do e-mail.
 * <p>
 * O ponto importante: essa porta é assíncrona por contrato — quem
 * chama {@code despachar} não espera o envio terminar. A
 * implementação (infraestrutura) é quem decide COMO isso acontece
 * (thread pool, fila, provedor de e-mail) e é ela quem, ao final,
 * atualiza o {@code EmailLog} correspondente para ENVIADO ou FALHOU.
 * <p>
 * Isso mantém os usecases síncronos e simples: eles só criam o log
 * como PENDENTE e disparam o despacho, sem se importar com a
 * mecânica assíncrona.
 */
public interface EmailDispatcherGateway {

    void despachar(EmailMessage mensagem, Long emailLogId);
}
