package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.VeterinarianAvailabilityRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "TB_DISPONIBILIDADE_VETERINARIO",
        uniqueConstraints = @UniqueConstraint(name = "uk_disponibilidade_vet_inicio", columnNames = {"veterinarian_id", "start_time"})
)
public class VeterinarianAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Veterinarian veterinarian;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    public VeterinarianAvailability(VeterinarianAvailabilityRequest request, Veterinarian veterinarian) {
        this.veterinarian = veterinarian;
        this.startTime = request.startTime();
        this.endTime = request.endTime();
    }
}

