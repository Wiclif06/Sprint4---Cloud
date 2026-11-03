package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.PetRequest;
import br.com.fiap.petjourney.dtos.response.PetResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Pet;
import br.com.fiap.petjourney.models.Tutor;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.PetRepository;
import br.com.fiap.petjourney.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository repository;
    private final TutorRepository tutorRepository;
    private final AuthenticatedUserService authenticatedUser;

    @Cacheable(
            value = "pets",
            key = "{@authenticatedUserService.username(), #name, #pageable.toString()}"
    )
    public Page<PetResponse> findAll(String name, Pageable pageable) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            Long tutorId = authenticatedUser.tutorId();
            if (name != null && !name.isBlank()) {
                return repository.findByTutorIdAndActiveTrueAndNameContainingIgnoreCase(tutorId, name, pageable).map(PetResponse::fromEntity);
            }
            return repository.findByTutorIdAndActiveTrue(tutorId, pageable).map(PetResponse::fromEntity);
        }

        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            Long clinicId = authenticatedUser.clinicId();
            if (name != null && !name.isBlank()) {
                return repository.findPatientsByClinicIdAndName(clinicId, name, pageable).map(PetResponse::fromEntity);
            }
            return repository.findPatientsByClinicId(clinicId, pageable).map(PetResponse::fromEntity);
        }

        if (name != null && !name.isBlank()) {
            return repository.findByActiveTrueAndNameContainingIgnoreCase(name, pageable).map(PetResponse::fromEntity);
        }

        return repository.findByActiveTrue(pageable).map(PetResponse::fromEntity);
    }

    @Cacheable(value = "pets", key = "{@authenticatedUserService.username(), 'id', #id}")
    public PetResponse findById(Long id) {
        return PetResponse.fromEntity(findAccessiblePet(id));
    }

    @CacheEvict(value = "pets", allEntries = true)
    public PetResponse create(PetRequest request) {
        Tutor tutor = resolveTutorForWrite(request.tutorId());
        return PetResponse.fromEntity(repository.save(new Pet(request, tutor)));
    }

    @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "medicalRecords", allEntries = true),
            @CacheEvict(value = "medications", allEntries = true)
    })
    public PetResponse update(Long id, PetRequest request) {
        Pet pet = findAccessiblePet(id);
        Tutor tutor = resolveTutorForWrite(request.tutorId());
        pet.updateFrom(request, tutor);
        return PetResponse.fromEntity(repository.save(pet));
    }

    @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "medicalRecords", allEntries = true),
            @CacheEvict(value = "medications", allEntries = true)
    })
    public void delete(Long id) {
        Pet pet = findAccessiblePet(id);
        pet.setActive(false);
        repository.save(pet);
    }

    public Pet findAccessiblePet(Long id) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            return repository.findByIdAndTutorIdAndActiveTrue(id, authenticatedUser.tutorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));
        }
        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            return repository.findPatientByIdAndClinicId(id, authenticatedUser.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado para esta clínica"));
        }
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));
    }

    private Tutor resolveTutorForWrite(Long requestTutorId) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            throw new ForbiddenOperationException("Tutor não pode cadastrar ou alterar pets; o cadastro é feito pela clínica");
        }

        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            if (requestTutorId == null) {
                throw new ForbiddenOperationException("O tutor é obrigatório para cadastro de pet pela clínica");
            }
            Tutor tutor = tutorRepository.findByIdAndActiveTrue(requestTutorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));
            if (tutor.getClinic() == null || !tutor.getClinic().getId().equals(authenticatedUser.clinicId())) {
                throw new ForbiddenOperationException("Clínica não pode alterar pet de tutor sem vínculo");
            }
            return tutor;
        }

        throw new ForbiddenOperationException("Perfil sem permissão para cadastrar ou alterar pets");
    }
}

