package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.VeterinarianAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarianAvailabilityResponse extends RepresentationModel<VeterinarianAvailabilityResponse> {
    private Long id;
    private Long veterinarianId;
    private String veterinarianName;
    private Long clinicId;
    private String clinicName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static VeterinarianAvailabilityResponse fromEntity(VeterinarianAvailability availability) {
        var veterinarian = availability.getVeterinarian();
        var clinic = veterinarian != null ? veterinarian.getClinic() : null;
        return VeterinarianAvailabilityResponse.builder()
                .id(availability.getId())
                .veterinarianId(veterinarian != null ? veterinarian.getId() : null)
                .veterinarianName(veterinarian != null ? veterinarian.getName() : null)
                .clinicId(clinic != null ? clinic.getId() : null)
                .clinicName(clinic != null ? clinic.getName() : null)
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .build();
    }
}

