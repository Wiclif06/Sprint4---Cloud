package br.com.fiap.petjourney.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clinics/99");

    @Test
    @DisplayName("ResourceNotFoundException vira 404 com mensagem e caminho")
    void handleResourceNotFound_retorna404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(new ResourceNotFoundException("Clinica nao encontrada"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Clinica nao encontrada");
        assertThat(response.getBody().getPath()).isEqualTo("/clinics/99");
    }

    @Test
    @DisplayName("ForbiddenOperationException vira 403")
    void handleForbiddenOperation_retorna403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleForbiddenOperation(new ForbiddenOperationException("Sem permissao"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).isEqualTo("Sem permissao");
    }

    @Test
    @DisplayName("ConflictOperationException vira 409")
    void handleConflictOperation_retorna409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflictOperation(new ConflictOperationException("Ja existe"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Ja existe");
    }

    @Test
    @DisplayName("CNPJ duplicado no banco vira 409 com mensagem amigavel")
    void handleDataIntegrityViolation_cnpjDuplicado_retorna409ComMensagem() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "erro",
                new RuntimeException("duplicate key value violates unique constraint \"tb_clinica_cnpj_key\""));
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(exception, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Ja existe clinica cadastrada com este CNPJ");
    }

    @Test
    @DisplayName("Excecao inesperada vira 500 sem vazar detalhes internos")
    void handleGeneric_retorna500ComMensagemGenerica() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(new IllegalStateException("detalhe interno"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Ocorreu um erro inesperado no servidor");
    }
}
