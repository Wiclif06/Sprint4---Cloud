package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterTutorWithPetRequest(
        @Valid
        @NotNull(message = "Os dados do tutor são obrigatórios")
        TutorRequest tutor,

        @Valid
        @NotNull(message = "Os dados do pet são obrigatórios")
        PetRequest pet
) {
}

