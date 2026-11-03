package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.CreateClinicAdminRequest;
import br.com.fiap.petjourney.dtos.response.UserAccountResponse;
import br.com.fiap.petjourney.services.SystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Tag(name = "Administração do Sistema", description = "Endpoints exclusivos do administrador geral")
public class SystemAdminController {

    private final SystemAdminService service;

    @PostMapping("/clinics/{clinicId}/admins")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    @Operation(summary = "Criar administrador para uma clínica")
    public ResponseEntity<UserAccountResponse> createClinicAdmin(
            @PathVariable Long clinicId,
            @RequestBody @Valid CreateClinicAdminRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createClinicAdmin(clinicId, request));
    }
}

