package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.VeterinarianRequest;
import br.com.fiap.petjourney.dtos.response.VeterinarianResponse;
import br.com.fiap.petjourney.services.VeterinarianService;
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
@RequestMapping("/veterinarians")
@RequiredArgsConstructor
@Tag(name = "Veterinário", description = "Endpoints para gerenciamento de veterinários")
public class VeterinarianController {

    private final VeterinarianService service;

    @GetMapping
    @Operation(summary = "Listar todos os veterinários com busca por nome e paginação")
    public ResponseEntity<Page<VeterinarianResponse>> getAll(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<VeterinarianResponse> veterinarians = service.findAll(name, pageable);
        veterinarians.forEach(vet -> vet.add(linkTo(methodOn(VeterinarianController.class).getById(vet.getId())).withSelfRel()));
        return ResponseEntity.ok(veterinarians);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veterinário por ID")
    public ResponseEntity<VeterinarianResponse> getById(@PathVariable Long id) {
        VeterinarianResponse vet = service.findById(id);
        vet.add(linkTo(methodOn(VeterinarianController.class).getById(id)).withSelfRel());
        vet.add(linkTo(methodOn(VeterinarianController.class).getAll(null, Pageable.unpaged())).withRel("all-veterinarians"));
        return ResponseEntity.ok(vet);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_CLINICA')")
    @Operation(summary = "Cadastrar novo veterinário")
    public ResponseEntity<VeterinarianResponse> create(@RequestBody @Valid VeterinarianRequest request) {
        VeterinarianResponse vet = service.create(request);
        vet.add(linkTo(methodOn(VeterinarianController.class).getById(vet.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(vet);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_CLINICA')")
    @Operation(summary = "Atualizar veterinário")
    public ResponseEntity<VeterinarianResponse> update(@PathVariable Long id, @RequestBody @Valid VeterinarianRequest request) {
        VeterinarianResponse vet = service.update(id, request);
        vet.add(linkTo(methodOn(VeterinarianController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(vet);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_CLINICA')")
    @Operation(summary = "Excluir veterinário")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

