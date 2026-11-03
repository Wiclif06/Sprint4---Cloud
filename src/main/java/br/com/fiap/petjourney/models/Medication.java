package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.MedicationRequest;
import br.com.fiap.petjourney.models.enums.MedicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_MEDICAMENTO")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String dosage;

    private String frequency;

    private LocalDate startDate;

    private LocalDate endDate;

    private String observations;

    @Enumerated(EnumType.STRING)
    private MedicationStatus status;

    @ManyToOne
    private Pet pet;

    @ManyToOne
    private Veterinarian veterinarian;

    @ManyToOne(fetch = FetchType.LAZY)
    private MedicalRecord medicalRecord;

    public Medication(MedicationRequest request, Pet pet, Veterinarian veterinarian) {
        this.name = request.name();
        this.dosage = request.dosage();
        this.frequency = request.frequency();
        this.startDate = request.startDate();
        this.endDate = request.endDate();
        this.observations = request.observations();
        this.status = request.status();
        this.pet = pet;
        this.veterinarian = veterinarian;
    }

    public void updateFrom(MedicationRequest request, Pet pet, Veterinarian veterinarian) {
        this.name = request.name();
        this.dosage = request.dosage();
        this.frequency = request.frequency();
        this.startDate = request.startDate();
        this.endDate = request.endDate();
        this.observations = request.observations();
        this.status = request.status();
        this.pet = pet;
        this.veterinarian = veterinarian;
    }
}

