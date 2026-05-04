package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Nomes das filas ────────────────────────────────────────────────────
    public static final String QUEUE_PACIENTE_CRIAR     = "paciente.criar";
    public static final String QUEUE_PACIENTE_CRIAR_DLQ = "paciente.criar.dlq";
    public static final String QUEUE_PACIENTE_CRIADO    = "paciente.criado";

    // ── Nomes das exchanges ───────────────────────────────────────────────
    public static final String EXCHANGE_PACIENTE        = "paciente.exchange";
    public static final String EXCHANGE_DLQ             = "dlq.exchange";

    // ── Routing keys ──────────────────────────────────────────────────────
    public static final String RK_PACIENTE_CRIAR        = "paciente.criar";
    public static final String RK_PACIENTE_CRIADO       = "paciente.criado";
    public static final String RK_PACIENTE_CRIAR_DLQ    = "paciente.criar.dlq";

    // ────────────────────────────────────────────────────────────────────────
    // Exchanges
    // ────────────────────────────────────────────────────────────────────────

    @Bean
    public DirectExchange pacienteExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_PACIENTE).durable(true).build();
    }

    /** Exchange dedicada a Dead Letter Queues — separada por domínio. */
    @Bean
    public DirectExchange dlqExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLQ).durable(true).build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Dead Letter Queue — declarada antes da fila principal
    // ────────────────────────────────────────────────────────────────────────

    @Bean
    public Queue pacienteCriarDlq() {
        return QueueBuilder.durable(QUEUE_PACIENTE_CRIAR_DLQ).build();
    }

    @Bean
    public Binding dlqBinding(Queue pacienteCriarDlq, DirectExchange dlqExchange) {
        return BindingBuilder.bind(pacienteCriarDlq)
                .to(dlqExchange)
                .with(RK_PACIENTE_CRIAR_DLQ);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Fila principal — redireciona para DLQ em caso de falha
    // ────────────────────────────────────────────────────────────────────────

    @Bean
    public Queue pacienteCriarQueue() {
        return QueueBuilder.durable(QUEUE_PACIENTE_CRIAR)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
                .withArgument("x-dead-letter-routing-key", RK_PACIENTE_CRIAR_DLQ)
                .build();
    }

    @Bean
    public Binding pacienteCriarBinding(Queue pacienteCriarQueue, DirectExchange pacienteExchange) {
        return BindingBuilder.bind(pacienteCriarQueue)
                .to(pacienteExchange)
                .with(RK_PACIENTE_CRIAR);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Fila de saída — declarada aqui para garantir durabilidade
    // mesmo que a api-triagem ainda não tenha subido
    // ────────────────────────────────────────────────────────────────────────

    @Bean
    public Queue pacienteCriadoQueue() {
        return QueueBuilder.durable(QUEUE_PACIENTE_CRIADO).build();
    }

    @Bean
    public Binding pacienteCriadoBinding(Queue pacienteCriadoQueue, DirectExchange pacienteExchange) {
        return BindingBuilder.bind(pacienteCriadoQueue)
                .to(pacienteExchange)
                .with(RK_PACIENTE_CRIADO);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Serialização JSON para todos os producers/consumers
    // ────────────────────────────────────────────────────────────────────────

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
