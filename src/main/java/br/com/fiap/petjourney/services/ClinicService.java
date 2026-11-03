package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.ClinicRequest;
import br.com.fiap.petjourney.dtos.response.ClinicResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Clinic;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.ClinicRepository;
import br.com.fiap.petjourney.repositories.PetRepository;
import br.com.fiap.petjourney.repositories.TutorRepository;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import br.com.fiap.petjourney.repositories.VeterinarianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository repository;
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatedUserService authenticatedUser;

    public Page<ClinicResponse> findAll(Pageable pageable) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            Clinic clinic = repository.findByIdAndActiveTrue(authenticatedUser.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinica autenticada nao encontrada"));
            return new PageImpl<>(List.of(ClinicResponse.fromEntity(clinic)), pageable, 1);
        }
        if (role == UserRole.TUTOR) {
            List<ClinicResponse> clinics = repository.findByTutorId(authenticatedUser.tutorId())
                    .stream()
                    .map(ClinicResponse::fromEntity)
                    .toList();
            return new PageImpl<>(clinics, pageable, clinics.size());
        }

        return repository.findByActiveTrue(pageable).map(ClinicResponse::fromEntity);
    }

    public ClinicResponse findById(Long id) {
        assertClinicAccess(id);
        return repository.findByIdAndActiveTrue(id)
                .map(ClinicResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Clinica nao encontrada"));
    }

    public ClinicResponse create(ClinicRequest request) {
        if (authenticatedUser.role() != UserRole.ADMIN_SISTEMA) {
            throw new ForbiddenOperationException("Apenas administradores do sistema podem cadastrar clinicas");
        }
        return ClinicResponse.fromEntity(repository.save(new Clinic(request)));
    }

    public ClinicResponse update(Long id, ClinicRequest request) {
        assertSystemAdmin();
        assertClinicAccess(id);
        Clinic clinic = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinica nao encontrada"));

        clinic.updateFrom(request);

        return ClinicResponse.fromEntity(repository.save(clinic));
    }

    @Transactional
    public void delete(Long id) {
        assertSystemAdmin();
        assertClinicAccess(id);

        Clinic clinic = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinica nao encontrada"));

        userAccountRepository.deactivateClinicAccounts(id);
        userAccountRepository.deactivateTutorAccountsByClinicId(id);
        userAccountRepository.deactivateVeterinarianAccountsByClinicId(id);
        petRepository.deactivateByClinicId(id);
        tutorRepository.deactivateByClinicId(id);
        veterinarianRepository.deactivateByClinicId(id);

        clinic.setActive(false);
        repository.save(clinic);
    }

    private void assertClinicAccess(Long clinicId) {
        UserRole role = authenticatedUser.role();
        if ((role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO)
                && !clinicId.equals(authenticatedUser.clinicId())) {
            throw new ForbiddenOperationException("Usuario nao pode acessar dados de outra clinica");
        }
        if (role == UserRole.TUTOR && !clinicId.equals(authenticatedUser.clinicId())) {
            throw new ForbiddenOperationException("Tutor nao pode acessar clinica sem vinculo");
        }
    }

    private void assertSystemAdmin() {
        if (authenticatedUser.role() != UserRole.ADMIN_SISTEMA) {
            throw new ForbiddenOperationException("Apenas administradores do sistema podem alterar clinicas");
        }
    }
}

