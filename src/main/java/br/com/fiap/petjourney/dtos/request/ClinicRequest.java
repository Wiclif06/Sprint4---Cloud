package br.com.fiap.petjourney.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record ClinicRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String name,

        @NotBlank(message = "O CNPJ e obrigatorio")
        @CNPJ(message = "CNPJ invalido")
        String cnpj,

        @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
        String phone,

        @Email(message = "E-mail invalido")
        @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
        String email,

        @Size(max = 255, message = "O endereco deve ter no maximo 255 caracteres")
        String address
) {
}

