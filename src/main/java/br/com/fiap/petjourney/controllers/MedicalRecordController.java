package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.MedicalRecordRequest;
import br.com.fiap.petjourney.dtos.response.MedicalRecordResponse;
import br.com.fiap.petjourney.services.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@Tag(name = "Prontuário", description = "Endpoints para gerenciamento de prontuários médicos")
public class MedicalRecordController {

    private final MedicalRecordService service;

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Listar histórico clínico de um pet com paginação e cache")
    public ResponseEntity<Page<MedicalRecordResponse>> getByPetId(
            @PathVariable Long petId,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<MedicalRecordResponse> records = service.findByPetId(petId, pageable);
        records.forEach(record -> record.add(linkTo(methodOn(MedicalRecordController.class).getById(record.getId())).withSelfRel()));
        return ResponseEntity.ok(records);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar prontuários por intervalo de data")
    public ResponseEntity<Page<MedicalRecordResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<MedicalRecordResponse> records = service.findByDateRange(start, end, pageable);
        records.forEach(record -> record.add(linkTo(methodOn(MedicalRecordController.class).getById(record.getId())).withSelfRel()));
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar prontuário por ID")
    public ResponseEntity<MedicalRecordResponse> getById(@PathVariable Long id) {
        MedicalRecordResponse record = service.findById(id);
        record.add(linkTo(methodOn(MedicalRecordController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(record);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Registrar novo prontuário")
    public ResponseEntity<MedicalRecordResponse> create(@RequestBody @Valid MedicalRecordRequest request) {
        MedicalRecordResponse record = service.create(request);
        record.add(linkTo(methodOn(MedicalRecordController.class).getById(record.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Atualizar prontuário")
    public ResponseEntity<MedicalRecordResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid MedicalRecordRequest request) {
        MedicalRecordResponse record = service.update(id, request);
        record.add(linkTo(methodOn(MedicalRecordController.class).getById(record.getId())).withSelfRel());
        return ResponseEntity.ok(record);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Excluir prontuário")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

