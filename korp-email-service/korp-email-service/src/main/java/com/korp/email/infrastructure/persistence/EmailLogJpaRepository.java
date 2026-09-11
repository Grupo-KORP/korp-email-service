package com.korp.email.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogJpaRepository extends JpaRepository<EmailLogJpaEntity, Long> {
}
