package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClinicAdminRequest(
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String username,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 6, max = 120, message = "A senha deve ter entre 6 e 120 caracteres")
        String password
) {
}

