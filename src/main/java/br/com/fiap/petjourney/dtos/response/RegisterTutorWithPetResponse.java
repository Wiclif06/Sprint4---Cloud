package br.com.fiap.petjourney.dtos.response;

public record RegisterTutorWithPetResponse(
        TutorResponse tutor,
        PetResponse pet,
        String firstAccessCode
) {
}

