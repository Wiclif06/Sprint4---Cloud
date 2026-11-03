package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VeterinarianRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String name,

        @NotBlank(message = "O CRMV e obrigatorio")
        @Size(max = 50, message = "O CRMV deve ter no maximo 50 caracteres")
        String crmv,

        @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
        String phone,

        @Email(message = "E-mail invalido")
        @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
        String email,

        @Size(max = 120, message = "A especialidade deve ter no maximo 120 caracteres")
        String specialty,

        @NotNull(message = "O ID da clinica e obrigatorio")
        @Positive(message = "O ID da clinica deve ser positivo")
        Long clinicId
) {
}

