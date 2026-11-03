package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.Medication;
import br.com.fiap.petjourney.models.enums.MedicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationResponse extends RepresentationModel<MedicationResponse> {
    private Long id;
    private String name;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observations;
    private MedicationStatus status;
    private String petName;
    private String veterinarianName;

    public static MedicationResponse fromEntity(Medication medication) {
        return MedicationResponse.builder()
                .id(medication.getId())
                .name(medication.getName())
                .dosage(medication.getDosage())
                .frequency(medication.getFrequency())
                .startDate(medication.getStartDate())
                .endDate(medication.getEndDate())
                .observations(medication.getObservations())
                .status(medication.getStatus())
                .petName(medication.getPet() != null ? medication.getPet().getName() : null)
                .veterinarianName(medication.getVeterinarian() != null ? medication.getVeterinarian().getName() : null)
                .build();
    }
}

