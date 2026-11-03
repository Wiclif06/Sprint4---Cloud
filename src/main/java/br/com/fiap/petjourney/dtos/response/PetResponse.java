package br.com.fiap.petjourney.dtos.response;

import br.com.fiap.petjourney.models.Pet;
import br.com.fiap.petjourney.models.enums.PetSex;
import br.com.fiap.petjourney.models.enums.PetSpecies;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse extends RepresentationModel<PetResponse> {
    private Long id;
    private String name;
    private PetSpecies species;
    private String breed;
    private PetSex sex;
    private LocalDate birthDate;
    private BigDecimal weight;
    private Long tutorId;
    private String tutorName;

    public static PetResponse fromEntity(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .sex(pet.getSex())
                .birthDate(pet.getBirthDate())
                .weight(pet.getWeight())
                .tutorId(pet.getTutor() != null ? pet.getTutor().getId() : null)
                .tutorName(pet.getTutor() != null ? pet.getTutor().getName() : null)
                .build();
    }
}

