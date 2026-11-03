package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.Clinic;
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
public class ClinicResponse extends RepresentationModel<ClinicResponse> {
    private Long id;
    private String name;
    private String cnpj;
    private String phone;
    private String email;
    private String address;

    public static ClinicResponse fromEntity(Clinic clinic) {
        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .cnpj(clinic.getCnpj())
                .phone(clinic.getPhone())
                .email(clinic.getEmail())
                .address(clinic.getAddress())
                .build();
    }
}

