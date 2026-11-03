package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record VeterinarianAvailabilityRequest(
        @Positive(message = "O ID do veterinario deve ser positivo")
        Long veterinarianId,

        @NotNull(message = "O inicio da disponibilidade e obrigatorio")
        @Future(message = "A disponibilidade deve ser futura")
        LocalDateTime startTime,

        @NotNull(message = "O fim da disponibilidade e obrigatorio")
        @Future(message = "O fim da disponibilidade deve ser futuro")
        LocalDateTime endTime
) {
}

