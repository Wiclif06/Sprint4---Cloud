package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.AppointmentRequest;
import br.com.fiap.petjourney.models.enums.AppointmentStatus;
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
@Table(name = "TB_AGENDAMENTO")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @ManyToOne
    private Pet pet;

    @ManyToOne
    private Veterinarian veterinarian;

    @ManyToOne
    private Clinic clinic;

    public Appointment(AppointmentRequest request, Pet pet, Veterinarian veterinarian, Clinic clinic) {
        this.title = request.title();
        this.description = request.description();
        this.dateTime = request.dateTime();
        this.status = request.status();
        this.pet = pet;
        this.veterinarian = veterinarian;
        this.clinic = clinic;
    }

    public void updateFrom(AppointmentRequest request, Pet pet, Veterinarian veterinarian, Clinic clinic) {
        this.title = request.title();
        this.description = request.description();
        this.dateTime = request.dateTime();
        this.status = request.status();
        this.pet = pet;
        this.veterinarian = veterinarian;
        this.clinic = clinic;
    }
}

