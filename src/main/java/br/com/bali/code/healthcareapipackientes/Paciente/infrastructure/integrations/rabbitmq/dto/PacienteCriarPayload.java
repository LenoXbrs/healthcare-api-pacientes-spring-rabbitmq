package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto;

/**
 * Payload consumido da fila paciente.criar.
 * Publicado pela api-triagem quando recebe nova solicitação do gateway.
 */
public record PacienteCriarPayload(
    String nome,
    String cpf,
    Long triagemId
) {}
