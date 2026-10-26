package com.korp.email.infrastructure.config;

import com.korp.email.domain.gateway.EmailLogRepository;
import com.korp.email.domain.model.EmailLog;
import com.korp.email.infrastructure.messaging.EmailQueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);
    private static final String ERRO_ENVIO = "Falha definitiva após três tentativas de envio.";

    @Bean
    public FanoutExchange emailExchange(@Value("${broker.exchange.name}") String exchangeName) {
        return new FanoutExchange(exchangeName, true, false);
    }

    @Bean
    public Queue emailQueue(
            @Value("${broker.queue.name}") String queueName,
            @Value("${broker.dead-letter-exchange.name}") String deadLetterExchangeName,
            @Value("${broker.dead-letter-routing-key}") String deadLetterRoutingKey) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    @Bean
    public Binding emailBinding(
            @Qualifier("emailQueue") Queue emailQueue,
            FanoutExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange);
    }

    @Bean
    public DirectExchange emailDeadLetterExchange(
            @Value("${broker.dead-letter-exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue emailDeadLetterQueue(@Value("${broker.dead-letter-queue.name}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding emailDeadLetterBinding(
            @Qualifier("emailDeadLetterQueue") Queue emailDeadLetterQueue,
            DirectExchange emailDeadLetterExchange,
            @Value("${broker.dead-letter-routing-key}") String routingKey) {
        return BindingBuilder.bind(emailDeadLetterQueue).to(emailDeadLetterExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.korp.email.infrastructure.messaging");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public RetryOperationsInterceptor emailRetryInterceptor(
            Jackson2JsonMessageConverter rabbitMessageConverter,
            EmailLogRepository emailLogRepository,
            @Value("${broker.retry.max-attempts:3}") int maxAttempts,
            @Value("${broker.retry.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${broker.retry.max-interval-ms:2000}") long maxIntervalMs,
            @Value("${broker.retry.multiplier:2.0}") double multiplier) {
        MessageRecoverer recoverer = (message, cause) -> {
            Object payload = rabbitMessageConverter.fromMessage(message);
            if (payload instanceof EmailQueueMessage queueMessage) {
                marcarFalhaFinal(emailLogRepository, queueMessage.emailLogId());
            } else {
                log.error("Mensagem de e-mail inválida encaminhada à DLQ após o limite de tentativas", cause);
            }
            throw new AmqpRejectAndDontRequeueException("Limite de tentativas excedido.", cause);
        };

        return RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts)
                .backOffOptions(initialIntervalMs, multiplier, maxIntervalMs)
                .recoverer(recoverer)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter rabbitMessageConverter,
            RetryOperationsInterceptor emailRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setAdviceChain(emailRetryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    private void marcarFalhaFinal(EmailLogRepository repository, Long emailLogId) {
        try {
            repository.buscarPorId(emailLogId).ifPresentOrElse(emailLog -> {
                emailLog.marcarComoFalha(ERRO_ENVIO);
                repository.salvar(emailLog);
            }, () -> log.warn("EmailLog {} não encontrado após falha definitiva", emailLogId));
        } catch (RuntimeException e) {
            log.error("Não foi possível atualizar o status do e-mail após falha definitiva (logId={})", emailLogId, e);
            throw e;
        }
    }
}
