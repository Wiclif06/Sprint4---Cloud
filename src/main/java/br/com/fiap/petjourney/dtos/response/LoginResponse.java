package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.enums.UserRole;

public record LoginResponse(
        String token,
        UserRole role,
        Long clinicId,
        Long tutorId,
        Long veterinarianId
) {
}

