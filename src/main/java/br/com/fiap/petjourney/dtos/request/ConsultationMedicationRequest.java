package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ConsultationMedicationRequest(
        @NotBlank(message = "O nome do medicamento e obrigatorio")
        @Size(max = 120, message = "O nome do medicamento deve ter no maximo 120 caracteres")
        String name,

        @Size(max = 120, message = "A dosagem deve ter no maximo 120 caracteres")
        String dosage,

        @Size(max = 120, message = "A frequencia deve ter no maximo 120 caracteres")
        String frequency,

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
        String observations
) {
}

