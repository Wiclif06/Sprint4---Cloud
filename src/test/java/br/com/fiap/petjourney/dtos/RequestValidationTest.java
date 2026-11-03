package br.com.fiap.petjourney.dtos;

import br.com.fiap.petjourney.dtos.request.ClinicRequest;
import br.com.fiap.petjourney.dtos.request.VeterinarianRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }

    @Test
    @DisplayName("ClinicRequest valido nao possui violacoes")
    void clinicRequest_dadosValidos_semViolacoes() {
        ClinicRequest request = new ClinicRequest(
                "Clinica Cloud", "11222333000181", "11999990001", "cloud@petjourney.com", "Avenida Paulista 1000");
        Set<ConstraintViolation<ClinicRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ClinicRequest rejeita CNPJ invalido")
    void clinicRequest_cnpjInvalido_violacaoNoCnpj() {
        ClinicRequest request = new ClinicRequest(
                "Clinica Cloud", "11111111111111", null, null, null);
        Set<ConstraintViolation<ClinicRequest>> violations = validator.validate(request);
        assertThat(hasViolationOn(violations, "cnpj")).isTrue();
    }

    @Test
    @DisplayName("ClinicRequest rejeita nome em branco e e-mail invalido")
    void clinicRequest_nomeEmBrancoEEmailInvalido_violacoes() {
        ClinicRequest request = new ClinicRequest(
                " ", "11222333000181", null, "email-invalido", null);
        Set<ConstraintViolation<ClinicRequest>> violations = validator.validate(request);
        assertThat(hasViolationOn(violations, "name")).isTrue();
        assertThat(hasViolationOn(violations, "email")).isTrue();
    }

    @Test
    @DisplayName("VeterinarianRequest valido nao possui violacoes")
    void veterinarianRequest_dadosValidos_semViolacoes() {
        VeterinarianRequest request = new VeterinarianRequest(
                "Dra Marina", "SP-90001", "11977770001", null, "Clinica Geral", 1L);
        Set<ConstraintViolation<VeterinarianRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("VeterinarianRequest exige CRMV e clinica")
    void veterinarianRequest_semCrmvEClinica_violacoes() {
        VeterinarianRequest request = new VeterinarianRequest(
                "Dra Marina", "", null, null, null, null);
        Set<ConstraintViolation<VeterinarianRequest>> violations = validator.validate(request);
        assertThat(hasViolationOn(violations, "crmv")).isTrue();
        assertThat(hasViolationOn(violations, "clinicId")).isTrue();
    }

    @Test
    @DisplayName("VeterinarianRequest rejeita ID de clinica negativo")
    void veterinarianRequest_clinicIdNegativo_violacao() {
        VeterinarianRequest request = new VeterinarianRequest(
                "Dra Marina", "SP-90001", null, null, null, -5L);
        Set<ConstraintViolation<VeterinarianRequest>> violations = validator.validate(request);
        assertThat(hasViolationOn(violations, "clinicId")).isTrue();
    }
}
