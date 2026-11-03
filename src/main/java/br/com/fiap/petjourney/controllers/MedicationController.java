package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.MedicationRequest;
import br.com.fiap.petjourney.dtos.response.MedicationResponse;
import br.com.fiap.petjourney.services.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
@Tag(name = "Medicamento", description = "Endpoints para prescrição de medicamentos")
public class MedicationController {

    private final MedicationService service;

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Listar medicamentos de um pet com paginação")
    public ResponseEntity<Page<MedicationResponse>> getByPetId(
            @PathVariable Long petId,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<MedicationResponse> medications = service.findByPetId(petId, pageable);
        medications.forEach(med -> med.add(linkTo(methodOn(MedicationController.class).getById(med.getId())).withSelfRel()));
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar medicamento por ID")
    public ResponseEntity<MedicationResponse> getById(@PathVariable Long id) {
        MedicationResponse med = service.findById(id);
        med.add(linkTo(methodOn(MedicationController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(med);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Prescrever novo medicamento")
    public ResponseEntity<MedicationResponse> create(@RequestBody @Valid MedicationRequest request) {
        MedicationResponse med = service.create(request);
        med.add(linkTo(methodOn(MedicationController.class).getById(med.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(med);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Atualizar medicamento")
    public ResponseEntity<MedicationResponse> update(@PathVariable Long id, @RequestBody @Valid MedicationRequest request) {
        MedicationResponse med = service.update(id, request);
        med.add(linkTo(methodOn(MedicationController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(med);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Excluir medicamento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

