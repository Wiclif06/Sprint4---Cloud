package br.com.fiap.petjourney.dtos.request;

import br.com.fiap.petjourney.models.enums.PetSex;
import br.com.fiap.petjourney.models.enums.PetSpecies;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PetRequest(
        @NotBlank(message = "O nome do pet e obrigatorio")
        @Size(max = 120, message = "O nome do pet deve ter no maximo 120 caracteres")
        String name,

        @NotNull(message = "A especie e obrigatoria")
        PetSpecies species,

        @Size(max = 120, message = "A raca deve ter no maximo 120 caracteres")
        String breed,

        PetSex sex,

        @PastOrPresent(message = "A data de nascimento nao pode ser futura")
        LocalDate birthDate,

        @Positive(message = "O peso deve ser positivo")
        BigDecimal weight,

        @Positive(message = "O ID do tutor deve ser positivo")
        Long tutorId
) {
}

