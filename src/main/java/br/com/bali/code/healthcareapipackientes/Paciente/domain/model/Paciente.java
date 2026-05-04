package br.com.bali.code.healthcareapipackientes.Paciente.domain.model;

import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "pacientes",
    indexes = {
        @Index(name = "idx_paciente_cpf", columnList = "cpf"),
        @Index(name = "idx_paciente_triagem_id", columnList = "triagem_id")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Paciente extends BaseAuditEntity {

    @Column(nullable = false)
    private String nome;

    /**
     * CPF armazenado apenas como string — sem formatação.
     * unique=true garante integridade no banco.
     */
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    /**
     * ID de referência cruzada com api-triagem.
     * NUNCA uma FK externa — cada serviço é dono dos seus dados.
     */
    @Column(name = "triagem_id")
    private Long triagemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPaciente status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
