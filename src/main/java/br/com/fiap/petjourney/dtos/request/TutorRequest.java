package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TutorRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String name,

        @NotBlank(message = "O CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 digitos")
        String cpf,

        @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
        String phone,

        @Email(message = "E-mail invalido")
        @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
        String email
) {
}

