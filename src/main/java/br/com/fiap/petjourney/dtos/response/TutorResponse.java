package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.Tutor;
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
public class TutorResponse extends RepresentationModel<TutorResponse> {
    private Long id;
    private String name;
    private String cpf;
    private String phone;
    private String email;
    private Long clinicId;
    private String clinicName;

    public static TutorResponse fromEntity(Tutor tutor) {
        return TutorResponse.builder()
                .id(tutor.getId())
                .name(tutor.getName())
                .cpf(tutor.getCpf())
                .phone(tutor.getPhone())
                .email(tutor.getEmail())
                .clinicId(tutor.getClinic() != null ? tutor.getClinic().getId() : null)
                .clinicName(tutor.getClinic() != null ? tutor.getClinic().getName() : null)
                .build();
    }
}

