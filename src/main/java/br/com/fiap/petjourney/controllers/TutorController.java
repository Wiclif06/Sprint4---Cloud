package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.TutorRequest;
import br.com.fiap.petjourney.dtos.response.TutorResponse;
import br.com.fiap.petjourney.services.TutorService;
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
@RequestMapping("/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutor", description = "Endpoints para consulta de tutores")
public class TutorController {

    private final TutorService service;

    @GetMapping
    @Operation(summary = "Listar todos os tutores com busca por nome e paginação")
    public ResponseEntity<Page<TutorResponse>> getAll(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<TutorResponse> tutors = service.findAll(name, pageable);
        tutors.forEach(tutor -> tutor.add(linkTo(methodOn(TutorController.class).getById(tutor.getId())).withSelfRel()));
        return ResponseEntity.ok(tutors);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    public ResponseEntity<TutorResponse> getById(@PathVariable Long id) {
        TutorResponse tutor = service.findById(id);
        tutor.add(linkTo(methodOn(TutorController.class).getById(id)).withSelfRel());
        tutor.add(linkTo(methodOn(TutorController.class).getAll(null, Pageable.unpaged())).withRel("all-tutors"));
        return ResponseEntity.ok(tutor);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Cadastrar novo tutor cliente da clínica")
    public ResponseEntity<TutorResponse> create(@RequestBody @Valid TutorRequest request) {
        TutorResponse tutor = service.create(request);
        tutor.add(linkTo(methodOn(TutorController.class).getById(tutor.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(tutor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Atualizar tutor")
    public ResponseEntity<TutorResponse> update(@PathVariable Long id, @RequestBody @Valid TutorRequest request) {
        TutorResponse tutor = service.update(id, request);
        tutor.add(linkTo(methodOn(TutorController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(tutor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_CLINICA')")
    @Operation(summary = "Excluir tutor cliente da clínica")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

