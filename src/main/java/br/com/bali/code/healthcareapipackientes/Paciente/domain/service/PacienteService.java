package br.com.bali.code.healthcareapipackientes.Paciente.domain.service;

import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.AtualizarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.response.PacienteResponse;
import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto.PacienteCriarPayload;

import java.util.List;

public interface PacienteService {

    /**
     * Cria um paciente a partir de um evento RabbitMQ.
     * Idempotente: se já existir um paciente com o mesmo triagemId,
     * republica o evento paciente.criado em vez de lançar erro.
     */
    void criarIdempotente(PacienteCriarPayload payload);

    PacienteResponse buscarPorId(Long id);

    PacienteResponse buscarPorCpf(String cpf);

    List<PacienteResponse> listarPorStatus(StatusPaciente status);

    PacienteResponse atualizar(Long id, AtualizarPacienteRequest request);
}
