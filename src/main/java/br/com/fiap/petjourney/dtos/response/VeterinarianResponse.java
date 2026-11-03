package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.Veterinarian;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarianResponse extends RepresentationModel<VeterinarianResponse> {
    private Long id;
    private String name;
    private String crmv;
    private String phone;
    private String email;
    private String specialty;
    private String clinicName;

    public static VeterinarianResponse fromEntity(Veterinarian veterinarian) {
        return VeterinarianResponse.builder()
                .id(veterinarian.getId())
                .name(veterinarian.getName())
                .crmv(veterinarian.getCrmv())
                .phone(veterinarian.getPhone())
                .email(veterinarian.getEmail())
                .specialty(veterinarian.getSpecialty())
                .clinicName(veterinarian.getClinic() != null ? veterinarian.getClinic().getName() : null)
                .build();
    }
}

