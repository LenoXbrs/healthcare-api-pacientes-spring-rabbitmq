package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto;

/**
 * Payload publicado na fila paciente.criado.
 * Consumido pela api-triagem para atualizar o estado da triagem com o pacienteId.
 */
public record PacienteCriadoPayload(
    Long pacienteId,
    Long triagemId
) {}
