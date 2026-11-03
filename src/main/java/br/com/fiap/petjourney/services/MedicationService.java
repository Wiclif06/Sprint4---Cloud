package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.MedicationRequest;
import br.com.fiap.petjourney.dtos.response.MedicationResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Medication;
import br.com.fiap.petjourney.models.Pet;
import br.com.fiap.petjourney.models.Veterinarian;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.MedicationRepository;
import br.com.fiap.petjourney.repositories.PetRepository;
import br.com.fiap.petjourney.repositories.VeterinarianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository repository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final AuthenticatedUserService authenticatedUser;

    @Cacheable(
            value = "medications",
            key = "{@authenticatedUserService.username(), 'pet', #petId, #pageable.toString()}"
    )
    public Page<MedicationResponse> findByPetId(Long petId, Pageable pageable) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            return repository.findByPetIdAndPetTutorId(petId, authenticatedUser.tutorId(), pageable)
                    .map(MedicationResponse::fromEntity);
        }
        if (role == UserRole.ADMIN_CLINICA) {
            return repository.findByPetIdAndVeterinarianClinicId(petId, authenticatedUser.clinicId(), pageable)
                    .map(MedicationResponse::fromEntity);
        }
        if (role == UserRole.VETERINARIO) {
            return repository.findByPetIdAndVeterinarianId(petId, authenticatedUser.veterinarianId(), pageable)
                    .map(MedicationResponse::fromEntity);
        }
        return repository.findByPetId(petId, pageable).map(MedicationResponse::fromEntity);
    }

    @Cacheable(value = "medications", key = "{@authenticatedUserService.username(), 'id', #id}")
    public MedicationResponse findById(Long id) {
        return MedicationResponse.fromEntity(findAccessibleMedication(id));
    }

    @CacheEvict(value = "medications", allEntries = true)
    public MedicationResponse create(MedicationRequest request) {
        validateMedicationPeriod(request.startDate(), request.endDate());

        Pet pet = petRepository.findByIdAndActiveTrue(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet nao encontrado"));

        Veterinarian veterinarian = veterinarianRepository.findByIdAndActiveTrue(request.veterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado"));

        assertWriteAccess(pet, veterinarian);
        return MedicationResponse.fromEntity(repository.save(new Medication(request, pet, veterinarian)));
    }

    @CacheEvict(value = "medications", allEntries = true)
    public MedicationResponse update(Long id, MedicationRequest request) {
        Medication medication = findAccessibleMedication(id);
        validateMedicationPeriod(request.startDate(), request.endDate());

        Pet pet = petRepository.findByIdAndActiveTrue(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet nao encontrado"));

        Veterinarian veterinarian = veterinarianRepository.findByIdAndActiveTrue(request.veterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado"));

        assertWriteAccess(pet, veterinarian);
        medication.updateFrom(request, pet, veterinarian);

        return MedicationResponse.fromEntity(repository.save(medication));
    }

    @CacheEvict(value = "medications", allEntries = true)
    public void delete(Long id) {
        Medication medication = findAccessibleMedication(id);
        assertWriteAccess(medication.getPet(), medication.getVeterinarian());
        repository.delete(medication);
    }

    @CacheEvict(value = "medications", allEntries = true)
    public Medication saveFromConsultation(Medication medication) {
        return repository.save(medication);
    }

    private Medication findAccessibleMedication(Long id) {
        Medication medication = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento nao encontrado"));

        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR && !medication.getPet().getTutor().getId().equals(authenticatedUser.tutorId())) {
            throw new ForbiddenOperationException("Tutor nao pode acessar medicamento de outro tutor");
        }
        if (role == UserRole.ADMIN_CLINICA && !medication.getVeterinarian().getClinic().getId().equals(authenticatedUser.clinicId())) {
            throw new ForbiddenOperationException("Administrador nao pode acessar medicamento de outra clinica");
        }
        if (role == UserRole.VETERINARIO && !medication.getVeterinarian().getId().equals(authenticatedUser.veterinarianId())) {
            throw new ForbiddenOperationException("Veterinario nao pode acessar medicamento de outro veterinario");
        }

        return medication;
    }

    private void assertWriteAccess(Pet pet, Veterinarian veterinarian) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            throw new ForbiddenOperationException("Tutor nao pode prescrever medicamento");
        }
        if (role == UserRole.ADMIN_SISTEMA) {
            throw new ForbiddenOperationException("Administrador do sistema nao pode prescrever medicamento");
        }
        if (veterinarian.getClinic() == null
                || pet.getTutor().getClinic() == null
                || !veterinarian.getClinic().getId().equals(pet.getTutor().getClinic().getId())) {
            throw new ForbiddenOperationException("Pet nao pertence a carteira de clientes da clinica");
        }
        if (role == UserRole.ADMIN_CLINICA && !veterinarian.getClinic().getId().equals(authenticatedUser.clinicId())) {
            throw new ForbiddenOperationException("Administrador nao pode prescrever em outra clinica");
        }
        if (role == UserRole.VETERINARIO && !veterinarian.getId().equals(authenticatedUser.veterinarianId())) {
            throw new ForbiddenOperationException("Veterinario nao pode prescrever para outro veterinario");
        }
    }

    private void validateMedicationPeriod(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ForbiddenOperationException("A data final do medicamento nao pode ser anterior a data inicial");
        }
    }
}

