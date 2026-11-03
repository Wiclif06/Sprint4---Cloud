package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ScheduleAppointmentRequest(
        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres")
        String title,

        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String description,

        @NotNull(message = "O ID do pet e obrigatorio")
        @Positive(message = "O ID do pet deve ser positivo")
        Long petId,

        @Positive(message = "O ID da clinica deve ser positivo")
        Long clinicId,

        @NotNull(message = "O ID do veterinario e obrigatorio")
        @Positive(message = "O ID do veterinario deve ser positivo")
        Long veterinarianId,

        @NotNull(message = "A data e hora sao obrigatorias")
        @Future(message = "O agendamento deve ser para uma data futura")
        LocalDateTime dateTime
) {
}

