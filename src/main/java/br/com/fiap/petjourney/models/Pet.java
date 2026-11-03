package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.PetRequest;
import br.com.fiap.petjourney.models.enums.PetSex;
import br.com.fiap.petjourney.models.enums.PetSpecies;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_PET")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private PetSpecies species;

    private String breed;

    @Enumerated(EnumType.STRING)
    private PetSex sex;

    private LocalDate birthDate;

    private BigDecimal weight;

    @ManyToOne
    private Tutor tutor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public Pet(PetRequest request, Tutor tutor) {
        this.name = request.name();
        this.species = request.species();
        this.breed = request.breed();
        this.sex = request.sex();
        this.birthDate = request.birthDate();
        this.weight = request.weight();
        this.tutor = tutor;
        this.active = true;
    }

    public void updateFrom(PetRequest request, Tutor tutor) {
        this.name = request.name();
        this.species = request.species();
        this.breed = request.breed();
        this.sex = request.sex();
        this.birthDate = request.birthDate();
        this.weight = request.weight();
        this.tutor = tutor;
    }
}

