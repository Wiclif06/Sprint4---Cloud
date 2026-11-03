package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MedicalRecordRequest(
        @NotNull(message = "A data de registro e obrigatoria")
        @PastOrPresent(message = "A data de registro nao pode ser futura")
        LocalDateTime registrationDate,

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

        @Positive(message = "O ID do agendamento deve ser positivo")
        Long appointmentId,

        @NotNull(message = "O ID do pet e obrigatorio")
        @Positive(message = "O ID do pet deve ser positivo")
        Long petId,

        @NotNull(message = "O ID do veterinario e obrigatorio")
        @Positive(message = "O ID do veterinario deve ser positivo")
        Long veterinarianId
) {
}

