package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.MedicalRecord;
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
public class MedicalRecordResponse extends RepresentationModel<MedicalRecordResponse> {
    private Long id;
    private LocalDateTime registrationDate;
    private String mainComplaint;
    private String diagnosis;
    private String conduct;
    private String observations;
    private String clinicalNotes;
    private String recommendations;
    private String prescriptionNotes;
    private Long appointmentId;
    private String petName;
    private String veterinarianName;
    private String clinicName;

    public static MedicalRecordResponse fromEntity(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .registrationDate(record.getRegistrationDate())
                .mainComplaint(record.getMainComplaint())
                .diagnosis(record.getDiagnosis())
                .conduct(record.getConduct())
                .observations(record.getObservations())
                .clinicalNotes(record.getClinicalNotes())
                .recommendations(record.getRecommendations())
                .prescriptionNotes(record.getPrescriptionNotes())
                .appointmentId(record.getAppointment() != null ? record.getAppointment().getId() : null)
                .petName(record.getPet() != null ? record.getPet().getName() : null)
                .veterinarianName(record.getVeterinarian() != null ? record.getVeterinarian().getName() : null)
                .clinicName(record.getVeterinarian() != null && record.getVeterinarian().getClinic() != null ? record.getVeterinarian().getClinic().getName() : null)
                .build();
    }
}

