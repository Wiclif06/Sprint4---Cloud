package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.VeterinarianAvailabilityRequest;
import br.com.fiap.petjourney.dtos.response.VeterinarianAvailabilityResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Veterinarian;
import br.com.fiap.petjourney.models.VeterinarianAvailability;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.VeterinarianAvailabilityRepository;
import br.com.fiap.petjourney.repositories.VeterinarianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VeterinarianAvailabilityService {

    private final VeterinarianAvailabilityRepository repository;
    private final VeterinarianRepository veterinarianRepository;
    private final AuthenticatedUserService authenticatedUser;

    public Page<VeterinarianAvailabilityResponse> findAvailableSlots(Long veterinarianId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        validateDateRange(start, end);

        Long clinicId = resolveClinicIdForRead();
        return repository.findAvailableSlots(clinicId, veterinarianId, start, end, pageable)
                .map(VeterinarianAvailabilityResponse::fromEntity);
    }

    public Page<VeterinarianAvailabilityResponse> findByVeterinarian(Long veterinarianId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        validateDateRange(start, end);

        Veterinarian veterinarian = veterinarianRepository.findByIdAndActiveTrue(veterinarianId)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado"));
        assertVeterinarianClinicAccess(veterinarian);
        return repository.findByVeterinarianIdAndStartTimeBetween(veterinarianId, start, end, pageable)
                .map(VeterinarianAvailabilityResponse::fromEntity);
    }

    public VeterinarianAvailabilityResponse create(VeterinarianAvailabilityRequest request) {
        Long veterinarianId = resolveVeterinarianIdForWrite(request.veterinarianId());
        Veterinarian veterinarian = veterinarianRepository.findByIdAndActiveTrue(veterinarianId)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado"));

        assertVeterinarianClinicAccess(veterinarian);
        if (!request.endTime().isAfter(request.startTime())) {
            throw new ForbiddenOperationException("O fim da disponibilidade deve ser depois do início");
        }
        if (repository.existsByVeterinarianIdAndStartTime(veterinarianId, request.startTime())) {
            throw new ForbiddenOperationException("Veterinário já possui disponibilidade neste horário");
        }

        return VeterinarianAvailabilityResponse.fromEntity(repository.save(new VeterinarianAvailability(request, veterinarian)));
    }

    public void delete(Long id) {
        var availability = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade não encontrada"));
        assertVeterinarianClinicAccess(availability.getVeterinarian());

        UserRole role = authenticatedUser.role();
        if (role == UserRole.VETERINARIO && !availability.getVeterinarian().getId().equals(authenticatedUser.veterinarianId())) {
            throw new ForbiddenOperationException("Veterinário só pode remover a própria disponibilidade");
        }

        repository.delete(availability);
    }

    public boolean isSlotAvailable(Long veterinarianId, LocalDateTime dateTime) {
        return repository.existsByVeterinarianIdAndStartTime(veterinarianId, dateTime);
    }

    private Long resolveClinicIdForRead() {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR || role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            return authenticatedUser.clinicId();
        }
        throw new ForbiddenOperationException("Perfil sem clínica não consulta horários disponíveis");
    }

    private Long resolveVeterinarianIdForWrite(Long requestVeterinarianId) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.VETERINARIO) {
            return authenticatedUser.veterinarianId();
        }
        if (role == UserRole.ADMIN_CLINICA) {
            if (requestVeterinarianId == null) {
                throw new ForbiddenOperationException("O veterinário é obrigatório para disponibilidade criada pela clínica");
            }
            return requestVeterinarianId;
        }
        throw new ForbiddenOperationException("Apenas clínica ou veterinário podem gerenciar disponibilidade");
    }

    private void assertVeterinarianClinicAccess(Veterinarian veterinarian) {
        Long clinicId = authenticatedUser.clinicId();
        if (veterinarian.getClinic() == null || !veterinarian.getClinic().getId().equals(clinicId)) {
            throw new ForbiddenOperationException("Usuário não pode acessar disponibilidade de outra clínica");
        }
    }
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ForbiddenOperationException("A data final nao pode ser anterior a data inicial");
        }
    }
}

