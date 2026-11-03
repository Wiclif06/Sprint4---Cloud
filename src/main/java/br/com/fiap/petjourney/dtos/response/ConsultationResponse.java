package br.com.fiap.petjourney.dtos.response;

import java.util.List;

public record ConsultationResponse(
        AppointmentResponse appointment,
        MedicalRecordResponse medicalRecord,
        List<MedicationResponse> medications
) {
}

