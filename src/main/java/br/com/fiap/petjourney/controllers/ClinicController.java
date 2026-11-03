package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.ClinicRequest;
import br.com.fiap.petjourney.dtos.response.ClinicResponse;
import br.com.fiap.petjourney.services.ClinicService;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clínica", description = "Endpoints para gerenciamento de clínicas")
public class ClinicController {

    private final ClinicService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    @Operation(summary = "Listar todas as clínicas com paginação")
    public ResponseEntity<Page<ClinicResponse>> getAll(@PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<ClinicResponse> clinics = service.findAll(pageable);
        clinics.forEach(clinic -> clinic.add(linkTo(methodOn(ClinicController.class).getById(clinic.getId())).withSelfRel()));
        return ResponseEntity.ok(clinics);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar clínica por ID")
    public ResponseEntity<ClinicResponse> getById(@PathVariable Long id) {
        ClinicResponse clinic = service.findById(id);
        clinic.add(linkTo(methodOn(ClinicController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(clinic);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    @Operation(summary = "Cadastrar nova clínica")
    public ResponseEntity<ClinicResponse> create(@RequestBody @Valid ClinicRequest request) {
        ClinicResponse clinic = service.create(request);
        clinic.add(linkTo(methodOn(ClinicController.class).getById(clinic.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(clinic);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    @Operation(summary = "Atualizar clínica")
    public ResponseEntity<ClinicResponse> update(@PathVariable Long id, @RequestBody @Valid ClinicRequest request) {
        ClinicResponse clinic = service.update(id, request);
        clinic.add(linkTo(methodOn(ClinicController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(clinic);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    @Operation(summary = "Excluir clínica")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

