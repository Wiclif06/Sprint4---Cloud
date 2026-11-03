package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConsultationRequest(
        @NotNull(message = "O ID do agendamento e obrigatorio")
        @Positive(message = "O ID do agendamento deve ser positivo")
        Long appointmentId,

        @NotBlank(message = "A queixa principal e obrigatoria")
        @Size(max = 500, message = "A queixa principal deve ter no maximo 500 caracteres")
        String mainComplaint,

        @Size(max = 500, message = "O diagnostico deve ter no maximo 500 caracteres")
        String diagnosis,

        @Size(max = 500, message = "A conduta deve ter no maximo 500 caracteres")
        String conduct,

        @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
        String observations,

        @Size(max = 1000, message = "As notas clinicas devem ter no maximo 1000 caracteres")
        String clinicalNotes,

        @Size(max = 1000, message = "As recomendacoes devem ter no maximo 1000 caracteres")
        String recommendations,

        @Size(max = 1000, message = "As notas de prescricao devem ter no maximo 1000 caracteres")
        String prescriptionNotes,

        @Valid
        List<ConsultationMedicationRequest> medications
) {
}

