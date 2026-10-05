package com.korp.email.infrastructure.mail;

import com.korp.email.domain.exception.EmailSendException;
import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.Attachment;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.domain.model.EmailMessage;
import com.korp.email.domain.model.EmailStatus;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Base64;

/** Envia o e-mail recebido pela fila e atualiza seu registro de domínio. */
@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);
    private static final String EMAIL_ORIGEM = "sistemacomissaotnd@gmail.com";
    private static final String NOME_ENVIADOR = "Sistema de Comissões/Vendas - TND";

    private final JavaMailSender javaMailSender;
    private final EmailLogRepository emailLogRepository;

    public EmailSenderService(JavaMailSender javaMailSender, EmailLogRepository emailLogRepository) {
        this.javaMailSender = javaMailSender;
        this.emailLogRepository = emailLogRepository;
    }

    public void enviar(EmailMessage mensagem, Long emailLogId) {
        EmailLog emailLog = emailLogRepository.buscarPorId(emailLogId)
                .orElseThrow(() -> new IllegalStateException("EmailLog " + emailLogId + " não encontrado."));

        if (emailLog.getStatus() == EmailStatus.ENVIADO) {
            log.info("Mensagem RabbitMQ ignorada porque o e-mail já foi enviado (logId={})", emailLogId);
            return;
        }

        try {
            javaMailSender.send(montarMimeMessage(mensagem));
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail recebido da fila (logId={})", emailLogId, e);
            throw new EmailSendException("Falha ao enviar o e-mail.", e);
        }

        emailLog.marcarComoEnviado();
        emailLogRepository.salvar(emailLog);
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
}
