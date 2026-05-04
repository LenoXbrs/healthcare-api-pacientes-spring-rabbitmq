package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.consumer;

import br.com.bali.code.healthcareapipackientes.Paciente.domain.service.PacienteService;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.config.RabbitMQConfig;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto.PacienteCriarPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PacienteCriarConsumer {

    private final PacienteService pacienteService;

    public PacienteCriarConsumer(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * Consome eventos da fila paciente.criar.
     *
     * Estratégia de erro (ackMode = AUTO — padrão Spring AMQP):
     * - Sucesso → mensagem confirmada (basicAck)
     * - Exceção não capturada → mensagem rejeitada (basicNack)
     *   → broker encaminha para paciente.criar.dlq via x-dead-letter-exchange
     *
     * NÃO fazemos try/catch genérico aqui — isso esconderia falhas reais
     * e impediria o roteamento correto para a DLQ.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PACIENTE_CRIAR)
    public void consume(PacienteCriarPayload payload) {
        log.info("[paciente.criar] Recebido: triagemId={} cpf={}",
                payload.triagemId(), payload.cpf());

        pacienteService.criarIdempotente(payload);

        log.info("[paciente.criar] Processado com sucesso: triagemId={}", payload.triagemId());
    }
}
