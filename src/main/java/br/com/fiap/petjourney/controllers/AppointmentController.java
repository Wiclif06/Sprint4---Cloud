package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.AppointmentRequest;
import br.com.fiap.petjourney.dtos.response.AppointmentResponse;
import br.com.fiap.petjourney.dtos.response.MedicalRecordResponse;
import br.com.fiap.petjourney.services.AppointmentService;
import br.com.fiap.petjourney.services.ConsultationReportPdfService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Agendamento", description = "Endpoints para agendamentos de consultas")
public class AppointmentController {

    private final AppointmentService service;
    private final MedicalRecordService medicalRecordService;
    private final ConsultationReportPdfService consultationReportPdfService;

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Listar agendamentos de um pet com paginação")
    public ResponseEntity<Page<AppointmentResponse>> getByPetId(
            @PathVariable Long petId,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<AppointmentResponse> appointments = service.findByPetId(petId, pageable);
        appointments.forEach(app -> app.add(linkTo(methodOn(AppointmentController.class).getById(app.getId())).withSelfRel()));
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        AppointmentResponse app = service.findById(id);
        app.add(linkTo(methodOn(AppointmentController.class).getById(id)).withSelfRel());
        app.add(linkTo(methodOn(AppointmentController.class).getByPetId(0L, Pageable.unpaged())).withRel("by-pet"));
        return ResponseEntity.ok(app);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar agendamentos por intervalo de data")
    public ResponseEntity<Page<AppointmentResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 10) @ParameterObject Pageable pageable) {
        Page<AppointmentResponse> appointments = service.findByDateRange(start, end, pageable);
        appointments.forEach(app -> app.add(linkTo(methodOn(AppointmentController.class).getById(app.getId())).withSelfRel()));
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    @Operation(summary = "Criar novo agendamento")
    public ResponseEntity<AppointmentResponse> create(@RequestBody @Valid AppointmentRequest request) {
        AppointmentResponse app = service.create(request);
        app.add(linkTo(methodOn(AppointmentController.class).getById(app.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(app);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar agendamento (incluindo status)")
    public ResponseEntity<AppointmentResponse> update(@PathVariable Long id, @RequestBody @Valid AppointmentRequest request) {
        AppointmentResponse app = service.update(id, request);
        app.add(linkTo(methodOn(AppointmentController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(app);
    }

    @GetMapping("/{id}/report")
    @Operation(summary = "Buscar relatório da consulta por agendamento")
    public ResponseEntity<MedicalRecordResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.findByAppointmentId(id));
    }

    @GetMapping("/{id}/report/pdf")
    @Operation(summary = "Exportar relatório da consulta em PDF")
    public ResponseEntity<byte[]> exportReportPdf(@PathVariable Long id) {
        byte[] pdf = medicalRecordService.generatePdfByAppointmentId(id, consultationReportPdfService);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consulta-" + id + "-relatorio.pdf\"")
                .body(pdf);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Marcar agendamento como concluído")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable Long id) {
        AppointmentResponse app = service.complete(id);
        app.add(linkTo(methodOn(AppointmentController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(app);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Desmarcar/cancelar agendamento com regra de 24 horas")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id) {
        AppointmentResponse app = service.cancel(id);
        app.add(linkTo(methodOn(AppointmentController.class).getById(id)).withSelfRel());
        return ResponseEntity.ok(app);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Excluir agendamento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

