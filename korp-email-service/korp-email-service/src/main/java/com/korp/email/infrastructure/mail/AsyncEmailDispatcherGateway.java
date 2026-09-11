package com.korp.email.infrastructure.mail;

import com.korp.email.domain.gateway.EmailDispatcherGateway;
import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.Attachment;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.domain.model.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Implementação assíncrona da porta {@link EmailDispatcherGateway}.
 * <p>
 * É aqui — e só aqui — que mora o detalhe técnico de "como" o e-mail
 * é enviado (JavaMailSender) e "como" o processamento é assíncrono
 * (@Async, thread pool configurado em AsyncConfig). O usecase que
 * chamou {@code despachar} já seguiu em frente há tempos; quando
 * este método termina (sucesso ou falha), ele mesmo atualiza o
 * EmailLog através da porta {@link EmailLogRepository}.
 */
@Component
public class AsyncEmailDispatcherGateway implements EmailDispatcherGateway {

    private static final Logger log = LoggerFactory.getLogger(AsyncEmailDispatcherGateway.class);

    private static final String EMAIL_ORIGEM = "sistemacomissaotnd@gmail.com";
    private static final String NOME_ENVIADOR = "Sistema de Comissões/Vendas - TND";

    private final JavaMailSender javaMailSender;
    private final EmailLogRepository emailLogRepository;

    public AsyncEmailDispatcherGateway(JavaMailSender javaMailSender, EmailLogRepository emailLogRepository) {
        this.javaMailSender = javaMailSender;
        this.emailLogRepository = emailLogRepository;
    }

    @Override
    @Async("emailTaskExecutor")
    public void despachar(EmailMessage mensagem, Long emailLogId) {
        try {
            MimeMessage mimeMessage = montarMimeMessage(mensagem);
            javaMailSender.send(mimeMessage);
            marcarComo(emailLogId, true, null);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail (logId={}) para {}", emailLogId, mensagem.destinatario(), e);
            marcarComo(emailLogId, false, e.getMessage());
        }
    }

    private MimeMessage montarMimeMessage(EmailMessage mensagem) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, mensagem.temAnexo(), "UTF-8");

        helper.setFrom(EMAIL_ORIGEM, NOME_ENVIADOR);
        helper.setTo(mensagem.destinatario());
        helper.setSubject(mensagem.assunto());
        helper.setText(mensagem.corpoHtml(), true);

        if (mensagem.temAnexo()) {
            Attachment anexo = mensagem.anexo();
            byte[] conteudo = Base64.getDecoder().decode(anexo.conteudoBase64());
            helper.addAttachment(anexo.nomeArquivo(), new ByteArrayResource(conteudo), anexo.contentType());
        }

        return mimeMessage;
    }

    private void marcarComo(Long emailLogId, boolean sucesso, String erro) {
        emailLogRepository.buscarPorId(emailLogId).ifPresentOrElse(emailLog -> {
            if (sucesso) {
                emailLog.marcarComoEnviado();
            } else {
                emailLog.marcarComoFalha(erro);
            }
            emailLogRepository.salvar(emailLog);
        }, () -> log.warn("EmailLog {} não encontrado ao tentar atualizar status", emailLogId));
    }
}
