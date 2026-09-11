package com.korp.email.domain.gateway;

import com.korp.email.domain.model.EmailLog;

import java.util.Optional;

/**
 * Porta de persistência do histórico de e-mails. A implementação
 * (JPA/Postgres) fica na infraestrutura — o domínio e a aplicação
 * não sabem como/onde isso é guardado.
 */
public interface EmailLogRepository {

    EmailLog salvar(EmailLog emailLog);

    Optional<EmailLog> buscarPorId(Long id);
}
