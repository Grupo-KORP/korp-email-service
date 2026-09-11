package com.korp.email.infrastructure.config;

import com.korp.email.application.usecase.EnviarPedidoDistribuidorUseCase;
import com.korp.email.application.usecase.EnviarSenhaProvisoriaUseCase;
import com.korp.email.application.usecase.EnviarTrocaSenhaUseCase;
import com.korp.email.domain.gateway.EmailDispatcherGateway;
import com.korp.email.domain.gateway.EmailLogRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * As classes de usecase (camada de aplicação) são propositalmente
 * "puras" — sem anotações do Spring. Quem sabe transformá-las em
 * beans e injetar as implementações das portas é a infraestrutura,
 * aqui.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public EnviarSenhaProvisoriaUseCase enviarSenhaProvisoriaUseCase(
            EmailLogRepository emailLogRepository, EmailDispatcherGateway emailDispatcherGateway) {
        return new EnviarSenhaProvisoriaUseCase(emailLogRepository, emailDispatcherGateway);
    }

    @Bean
    public EnviarTrocaSenhaUseCase enviarTrocaSenhaUseCase(
            EmailLogRepository emailLogRepository, EmailDispatcherGateway emailDispatcherGateway) {
        return new EnviarTrocaSenhaUseCase(emailLogRepository, emailDispatcherGateway);
    }

    @Bean
    public EnviarPedidoDistribuidorUseCase enviarPedidoDistribuidorUseCase(
            EmailLogRepository emailLogRepository, EmailDispatcherGateway emailDispatcherGateway) {
        return new EnviarPedidoDistribuidorUseCase(emailLogRepository, emailDispatcherGateway);
    }
}
