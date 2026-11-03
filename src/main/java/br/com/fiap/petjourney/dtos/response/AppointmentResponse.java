package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.Appointment;
import br.com.fiap.petjourney.models.enums.AppointmentStatus;
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
public class AppointmentResponse extends RepresentationModel<AppointmentResponse> {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private String petName;
    private String veterinarianName;
    private String clinicName;

    public static AppointmentResponse fromEntity(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .title(appointment.getTitle())
                .description(appointment.getDescription())
                .dateTime(appointment.getDateTime())
                .status(appointment.getStatus())
                .petName(appointment.getPet() != null ? appointment.getPet().getName() : null)
                .veterinarianName(appointment.getVeterinarian() != null ? appointment.getVeterinarian().getName() : null)
                .clinicName(appointment.getClinic() != null ? appointment.getClinic().getName() : null)
                .build();
    }
}

