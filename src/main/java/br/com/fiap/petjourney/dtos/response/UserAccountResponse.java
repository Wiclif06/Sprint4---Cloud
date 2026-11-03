package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.enums.UserRole;

public record UserAccountResponse(
        Long id,
        String username,
        UserRole role,
        Boolean active,
        Long clinicId,
        Long tutorId,
        Long veterinarianId
) {
    public static UserAccountResponse fromEntity(UserAccount user) {
        Long clinicId = user.getClinic() != null ? user.getClinic().getId() : null;
        if (clinicId == null && user.getTutor() != null && user.getTutor().getClinic() != null) {
            clinicId = user.getTutor().getClinic().getId();
        }
        if (clinicId == null && user.getVeterinarian() != null && user.getVeterinarian().getClinic() != null) {
            clinicId = user.getVeterinarian().getClinic().getId();
        }

        return new UserAccountResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                clinicId,
                user.getTutor() != null ? user.getTutor().getId() : null,
                user.getVeterinarian() != null ? user.getVeterinarian().getId() : null
        );
    }
}

