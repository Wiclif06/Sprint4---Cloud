package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.VeterinarianAvailabilityRequest;
import br.com.fiap.petjourney.dtos.response.VeterinarianAvailabilityResponse;
import br.com.fiap.petjourney.services.VeterinarianAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/veterinarian-availabilities")
@RequiredArgsConstructor
@Tag(name = "Disponibilidade Veterinária", description = "Horários disponíveis para agendamento")
public class VeterinarianAvailabilityController {

    private final VeterinarianAvailabilityService service;

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Listar horários disponíveis da clínica")
    public ResponseEntity<Page<VeterinarianAvailabilityResponse>> getAvailableSlots(
            @RequestParam(required = false) Long veterinarianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAvailableSlots(veterinarianId, start, end, pageable));
    }

    @GetMapping("/veterinarian/{veterinarianId}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Listar disponibilidades cadastradas de um veterinário")
    public ResponseEntity<Page<VeterinarianAvailabilityResponse>> getByVeterinarian(
            @PathVariable Long veterinarianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(service.findByVeterinarian(veterinarianId, start, end, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Cadastrar horário disponível")
    public ResponseEntity<VeterinarianAvailabilityResponse> create(@RequestBody @Valid VeterinarianAvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Excluir disponibilidade")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

