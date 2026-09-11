package com.korp.email.infrastructure.persistence;

import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.EmailLog;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementa a porta {@link EmailLogRepository} do domínio usando
 * Spring Data JPA. É a única classe que sabe converter entre o
 * modelo de domínio (EmailLog) e a entidade JPA.
 */
@Component
public class EmailLogRepositoryAdapter implements EmailLogRepository {

    private final EmailLogJpaRepository jpaRepository;

    public EmailLogRepositoryAdapter(EmailLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmailLog salvar(EmailLog emailLog) {
        EmailLogJpaEntity entity = new EmailLogJpaEntity(
                emailLog.getId(),
                emailLog.getTipo(),
                emailLog.getDestinatario(),
                emailLog.getStatus(),
                emailLog.getMensagemErro(),
                emailLog.getCriadoEm(),
                emailLog.getAtualizadoEm()
        );

        EmailLogJpaEntity salvo = jpaRepository.save(entity);

        return paraDominio(salvo);
    }

    @Override
    public Optional<EmailLog> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::paraDominio);
    }

    private EmailLog paraDominio(EmailLogJpaEntity entity) {
        return EmailLog.reconstruir(
                entity.getId(),
                entity.getTipo(),
                entity.getDestinatario(),
                entity.getStatus(),
                entity.getMensagemErro(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
