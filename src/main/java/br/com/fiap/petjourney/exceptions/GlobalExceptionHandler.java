package br.com.fiap.petjourney.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
                return;
            }
            validationErrors.put(error.getObjectName(), error.getDefaultMessage());
        });

        ErrorResponse error = baseError(HttpStatus.BAD_REQUEST, "Erro de validacao nos campos", request)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        ErrorResponse error = baseError(HttpStatus.BAD_REQUEST, "Erro de validacao nos parametros", request)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenOperation(
            ForbiddenOperationException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictOperationException.class)
    public ResponseEntity<ErrorResponse> handleConflictOperation(
            ConflictOperationException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.FORBIDDEN, "Acesso negado para o perfil autenticado", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, badRequestMessage(ex), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, dataIntegrityMessage(ex), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado ao processar {}", request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado no servidor", request);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(baseError(status, message, request).build());
    }

    private ErrorResponse.ErrorResponseBuilder baseError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI());
    }

    private String badRequestMessage(Exception ex) {
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Parametro invalido: " + mismatch.getName();
        }
        if (ex instanceof MissingServletRequestParameterException missing) {
            return "Parametro obrigatorio ausente: " + missing.getParameterName();
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "Corpo da requisicao invalido";
        }
        return ex.getMessage() != null ? ex.getMessage() : "Requisicao invalida";
    }

    private String dataIntegrityMessage(DataIntegrityViolationException ex) {
        String rootMessage = rootMessage(ex).toLowerCase();

        if (rootMessage.contains("tb_tutor_cpf_key")) {
            return "Ja existe tutor cadastrado com este CPF";
        }
        if (rootMessage.contains("tb_usuario_username_key")) {
            return "Ja existe usuario cadastrado com este e-mail";
        }
        if (rootMessage.contains("tb_clinica_cnpj_key")) {
            return "Ja existe clinica cadastrada com este CNPJ";
        }
        if (rootMessage.contains("foreign key") || rootMessage.contains("violates foreign key constraint")) {
            return "Nao foi possivel concluir a operacao porque existem registros vinculados";
        }
        if (rootMessage.contains("duplicate key")) {
            return "Registro duplicado";
        }

        return "Violacao de integridade dos dados";
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : "";
    }
}

