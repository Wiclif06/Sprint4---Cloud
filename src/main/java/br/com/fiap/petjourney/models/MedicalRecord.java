package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.MedicalRecordRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_PRONTUARIO")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private String mainComplaint;

    private String diagnosis;

    private String conduct;

    private String observations;

    @Column(length = 2000)
    private String clinicalNotes;

    @Column(length = 2000)
    private String recommendations;

    @Column(length = 2000)
    private String prescriptionNotes;

    @OneToOne(fetch = FetchType.LAZY)
    private Appointment appointment;

    @ManyToOne
    private Pet pet;

    @ManyToOne
    private Veterinarian veterinarian;

    public MedicalRecord(MedicalRecordRequest request, Pet pet, Veterinarian veterinarian) {
        this.registrationDate = request.registrationDate();
        this.mainComplaint = request.mainComplaint();
        this.diagnosis = request.diagnosis();
        this.conduct = request.conduct();
        this.observations = request.observations();
        this.clinicalNotes = request.clinicalNotes();
        this.recommendations = request.recommendations();
        this.prescriptionNotes = request.prescriptionNotes();
        this.appointment = null;
        this.pet = pet;
        this.veterinarian = veterinarian;
    }

    public void updateFrom(MedicalRecordRequest request, Pet pet, Veterinarian veterinarian) {
        this.registrationDate = request.registrationDate();
        this.mainComplaint = request.mainComplaint();
        this.diagnosis = request.diagnosis();
        this.conduct = request.conduct();
        this.observations = request.observations();
        this.clinicalNotes = request.clinicalNotes();
        this.recommendations = request.recommendations();
        this.prescriptionNotes = request.prescriptionNotes();
        this.pet = pet;
        this.veterinarian = veterinarian;
    }
}

