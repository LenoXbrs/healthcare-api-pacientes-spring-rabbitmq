package br.com.bali.code.healthcareapipackientes.Paciente.domain.service;

import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.AtualizarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.response.PacienteResponse;
import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import br.com.bali.code.healthcareapipackientes.Paciente.domain.model.Paciente;
import br.com.bali.code.healthcareapipackientes.Paciente.domain.repository.PacienteRepository;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.exception.PacienteNaoEncontradoException;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto.PacienteCriarPayload;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto.PacienteCriadoPayload;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.publisher.PacienteCriadoPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PacienteServiceImp implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteCriadoPublisher publisher;

    public PacienteServiceImp(PacienteRepository pacienteRepository,
                               PacienteCriadoPublisher publisher) {
        this.pacienteRepository = pacienteRepository;
        this.publisher = publisher;
    }

    /**
     * Criação idempotente via evento RabbitMQ.
     *
     * Cenários tratados:
     * 1. Primeira vez — cria o paciente e publica paciente.criado.
     * 2. Mensagem duplicada (triagemId já existe) — republica paciente.criado
     *    sem criar duplicata, garantindo at-least-once delivery.
     * 3. Mensagens fora de ordem — se chegarmos aqui com dados diferentes
     *    para o mesmo triagemId, o primeiro vence (idempotência conservadora).
     */
    @Override
    @Transactional
    public void criarIdempotente(PacienteCriarPayload payload) {
        Optional<Paciente> existente = pacienteRepository.findByTriagemId(payload.triagemId());

        if (existente.isPresent()) {
            log.warn("[idempotencia] Paciente para triagemId={} já existe (id={}). Republicando evento.",
                    payload.triagemId(), existente.get().getId());
            publisher.publicar(new PacienteCriadoPayload(
                    existente.get().getId(),
                    existente.get().getTriagemId()
            ));
            return;
        }

        Paciente paciente = Paciente.builder()
                .nome(payload.nome())
                .cpf(payload.cpf())
                .triagemId(payload.triagemId())
                .status(StatusPaciente.AGUARDANDO_TRIAGEM)
                .ativo(true)
                .build();

        Paciente salvo = pacienteRepository.save(paciente);
        log.info("[paciente.criar] Paciente criado: id={} triagemId={}", salvo.getId(), salvo.getTriagemId());

        publisher.publicar(new PacienteCriadoPayload(salvo.getId(), salvo.getTriagemId()));
    }

    @Override
    @Cacheable(value = "pacientes", key = "#id")
    public PacienteResponse buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PacienteNaoEncontradoException(id));
    }

    @Override
    public PacienteResponse buscarPorCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .map(this::toResponse)
                .orElseThrow(() -> new PacienteNaoEncontradoException("CPF: " + cpf));
    }

    @Override
    @Cacheable(value = "pacientes-status", key = "#status")
    public List<PacienteResponse> listarPorStatus(StatusPaciente status) {
        return pacienteRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "pacientes", key = "#id"),
        @CacheEvict(value = "pacientes-status", allEntries = true)
    })
    public PacienteResponse atualizar(Long id, AtualizarPacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNaoEncontradoException(id));

        paciente.setNome(request.nome());
        paciente.setStatus(request.status());
        paciente.setAtivo(request.ativo());

        return toResponse(pacienteRepository.save(paciente));
    }

    private PacienteResponse toResponse(Paciente p) {
        return new PacienteResponse(
                p.getId(),
                p.getNome(),
                p.getCpf(),
                p.getTriagemId(),
                p.getStatus(),
                p.getAtivo(),
                p.getCreatedAt()
        );
    }
}
