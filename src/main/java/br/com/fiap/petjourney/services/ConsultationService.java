package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.ConsultationMedicationRequest;
import br.com.fiap.petjourney.dtos.request.ConsultationRequest;
import br.com.fiap.petjourney.dtos.response.AppointmentResponse;
import br.com.fiap.petjourney.dtos.response.ConsultationResponse;
import br.com.fiap.petjourney.dtos.response.MedicalRecordResponse;
import br.com.fiap.petjourney.dtos.response.MedicationResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.models.MedicalRecord;
import br.com.fiap.petjourney.models.Medication;
import br.com.fiap.petjourney.models.enums.AppointmentStatus;
import br.com.fiap.petjourney.models.enums.MedicationStatus;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordService medicalRecordService;
    private final MedicationService medicationService;
    private final AuthenticatedUserService authenticatedUser;

    @Transactional
    public ConsultationResponse register(ConsultationRequest request) {
        var appointment = appointmentService.findAccessibleAppointment(request.appointmentId());
        if (appointment.getStatus() == AppointmentStatus.CANCELADO) {
            throw new ForbiddenOperationException("Não é possível registrar consulta de agendamento cancelado");
        }
        if (authenticatedUser.role() == UserRole.TUTOR) {
            throw new ForbiddenOperationException("Tutor não pode registrar consulta clínica");
        }
        if (medicalRecordService.existsByAppointmentId(appointment.getId())) {
            throw new ForbiddenOperationException("Esta consulta já possui relatório registrado");
        }

        validateMedicationPeriods(request.medications());

        var medicalRecord = MedicalRecord.builder()
                .registrationDate(LocalDateTime.now())
                .mainComplaint(request.mainComplaint())
                .diagnosis(request.diagnosis())
                .conduct(request.conduct())
                .observations(request.observations())
                .clinicalNotes(request.clinicalNotes())
                .recommendations(request.recommendations())
                .prescriptionNotes(request.prescriptionNotes())
                .appointment(appointment)
                .pet(appointment.getPet())
                .veterinarian(appointment.getVeterinarian())
                .build();

        var savedRecord = medicalRecordService.saveFromConsultation(medicalRecord);

        List<MedicationResponse> medications = (request.medications() == null ? List.<MedicationResponse>of() :
                request.medications().stream()
                        .map(item -> Medication.builder()
                                .name(item.name())
                                .dosage(item.dosage())
                                .frequency(item.frequency())
                                .startDate(item.startDate())
                                .endDate(item.endDate())
                                .observations(item.observations())
                                .status(MedicationStatus.ATIVO)
                                .medicalRecord(savedRecord)
                                .pet(appointment.getPet())
                                .veterinarian(appointment.getVeterinarian())
                                .build())
                        .map(medicationService::saveFromConsultation)
                        .map(MedicationResponse::fromEntity)
                        .toList()
        );

        appointment.setStatus(AppointmentStatus.REALIZADO);
        var savedAppointment = appointmentRepository.save(appointment);

        return new ConsultationResponse(
                AppointmentResponse.fromEntity(savedAppointment),
                MedicalRecordResponse.fromEntity(savedRecord),
                medications
        );
    }

    private void validateMedicationPeriods(List<ConsultationMedicationRequest> medications) {
        if (medications == null) {
            return;
        }
        medications.forEach(item -> {
            if (item.startDate() != null && item.endDate() != null && item.endDate().isBefore(item.startDate())) {
                throw new ForbiddenOperationException("A data final do medicamento nao pode ser anterior a data inicial");
            }
        });
    }
}

