package br.com.bali.code.healthcareapipackientes.Paciente.api.resource;

import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.AtualizarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.CriarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.response.PacienteResponse;
import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import br.com.bali.code.healthcareapipackientes.Paciente.domain.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
@Tag(name = "Pacientes", description = "Endpoints consumidos exclusivamente pelo API Gateway")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }


    @PostMapping
    @Operation(summary = "Criar paciente")
    public ResponseEntity<PacienteResponse> criar(@RequestBody CriarPacienteRequest request) {
        PacienteResponse paciente = pacienteService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar paciente por CPF")
    public ResponseEntity<PacienteResponse> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(pacienteService.buscarPorCpf(cpf));
    }

    @GetMapping
    @Operation(summary = "Listar pacientes por status")
    public ResponseEntity<List<PacienteResponse>> listarPorStatus(@RequestParam StatusPaciente status) {
        return ResponseEntity.ok(pacienteService.listarPorStatus(status));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do paciente")
    public ResponseEntity<PacienteResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarPacienteRequest request) {
        return ResponseEntity.ok(pacienteService.atualizar(id, request));
    }
}
