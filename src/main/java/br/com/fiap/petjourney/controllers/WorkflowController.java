package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.ConsultationRequest;
import br.com.fiap.petjourney.dtos.request.RegisterTutorWithPetRequest;
import br.com.fiap.petjourney.dtos.request.ScheduleAppointmentRequest;
import br.com.fiap.petjourney.dtos.response.AppointmentResponse;
import br.com.fiap.petjourney.dtos.response.ConsultationResponse;
import br.com.fiap.petjourney.dtos.response.RegisterTutorWithPetResponse;
import br.com.fiap.petjourney.services.AppointmentService;
import br.com.fiap.petjourney.services.ConsultationService;
import br.com.fiap.petjourney.services.TutorPetRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
@Tag(name = "Atendimento", description = "Fluxos completos de agendamento e consulta")
public class WorkflowController {

    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final TutorPetRegistrationService tutorPetRegistrationService;

    @PostMapping("/appointments/schedule")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Fluxo completo de agendamento")
    public ResponseEntity<AppointmentResponse> schedule(@RequestBody @Valid ScheduleAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.schedule(request));
    }

    @PostMapping("/consultations/register")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Fluxo completo de consulta com prontuário e medicações")
    public ResponseEntity<ConsultationResponse> registerConsultation(@RequestBody @Valid ConsultationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.register(request));
    }

    @PostMapping("/tutors/register-with-pet")
    @PreAuthorize("hasAnyRole('ADMIN_CLINICA', 'VETERINARIO')")
    @Operation(summary = "Cadastrar tutor com primeiro pet e gerar primeiro acesso")
    public ResponseEntity<RegisterTutorWithPetResponse> registerTutorWithPet(@RequestBody @Valid RegisterTutorWithPetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorPetRegistrationService.register(request));
    }
}

