package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.TutorRequest;
import br.com.fiap.petjourney.dtos.response.TutorResponse;
import br.com.fiap.petjourney.exceptions.ConflictOperationException;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Clinic;
import br.com.fiap.petjourney.models.Tutor;
import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.ClinicRepository;
import br.com.fiap.petjourney.repositories.PetRepository;
import br.com.fiap.petjourney.repositories.TutorRepository;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository repository;
    private final ClinicRepository clinicRepository;
    private final PetRepository petRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticatedUserService authenticatedUser;
    private final SecureRandom secureRandom = new SecureRandom();

    public Page<TutorResponse> findAll(String name, Pageable pageable) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            return repository.findByIdAndActiveTrue(authenticatedUser.tutorId())
                    .map(tutor -> new org.springframework.data.domain.PageImpl<>(java.util.List.of(TutorResponse.fromEntity(tutor)), pageable, 1))
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor autenticado não encontrado"));
        }

        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            Long clinicId = authenticatedUser.clinicId();
            if (name != null && !name.isBlank()) {
                return repository.findClientsByClinicIdAndName(clinicId, name, pageable).map(TutorResponse::fromEntity);
            }
            return repository.findClientsByClinicId(clinicId, pageable).map(TutorResponse::fromEntity);
        }

        if (name != null && !name.isBlank()) {
            return repository.findByActiveTrueAndNameContainingIgnoreCase(name, pageable).map(TutorResponse::fromEntity);
        }

        return repository.findByActiveTrue(pageable).map(TutorResponse::fromEntity);
    }

    public TutorResponse findById(Long id) {
        return TutorResponse.fromEntity(findAccessibleTutor(id));
    }

    @Transactional
    public TutorResponse create(TutorRequest request) {
        Tutor tutor = createTutorForAuthenticatedClinic(request);
        return TutorResponse.fromEntity(tutor);
    }

    @Transactional
    public TutorResponse update(Long id, TutorRequest request) {
        Tutor tutor = findAccessibleTutor(id);
        if (authenticatedUser.role() != UserRole.TUTOR
                && authenticatedUser.role() != UserRole.ADMIN_CLINICA
                && authenticatedUser.role() != UserRole.VETERINARIO) {
            throw new ForbiddenOperationException("Perfil sem permissão para atualizar tutor");
        }
        if (authenticatedUser.role() == UserRole.TUTOR && !Objects.equals(tutor.getCpf(), request.cpf())) {
            throw new ForbiddenOperationException("Tutor nao pode alterar o proprio CPF");
        }
        assertEmailNotChanged(tutor, request.email());
        assertCpfAvailableForUpdate(tutor, request.cpf());
        tutor.updateFrom(request);
        return TutorResponse.fromEntity(repository.save(tutor));
    }

    @Transactional
    public void delete(Long id) {
        Tutor tutor = findAccessibleTutor(id);
        if (authenticatedUser.role() != UserRole.ADMIN_CLINICA) {
            throw new ForbiddenOperationException("Apenas a clínica pode excluir tutores clientes");
        }
        userAccountRepository.deactivateTutorAccount(tutor.getId());
        petRepository.deactivateByTutorId(tutor.getId());
        tutor.setActive(false);
        repository.save(tutor);
    }

    public boolean isLinkedToAuthenticatedClinic(Long tutorId) {
        Long clinicId = authenticatedUser.clinicId();
        return repository.findClientByIdAndClinicId(tutorId, clinicId).isPresent();
    }

    @Transactional
    public Tutor createTutorForAuthenticatedClinic(TutorRequest request) {
        assertClinicStaff();
        assertCpfAvailableForCreate(request.cpf());
        assertUsernameAvailable(request.email());

        Clinic clinic = clinicRepository.findByIdAndActiveTrue(authenticatedUser.clinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Clínica autenticada não encontrada"));

        Tutor tutor = repository.save(new Tutor(request, clinic));
        createInactiveTutorAccess(tutor);
        return tutor;
    }

    private Tutor findAccessibleTutor(Long id) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.TUTOR) {
            if (!id.equals(authenticatedUser.tutorId())) {
                throw new ForbiddenOperationException("Tutor não pode acessar dados de outro tutor");
            }
            return repository.findByIdAndActiveTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));
        }

        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            return repository.findClientByIdAndClinicId(id, authenticatedUser.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado para esta clínica"));
        }

        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));
    }

    private void createInactiveTutorAccess(Tutor tutor) {
        if (tutor.getEmail() == null || tutor.getEmail().isBlank()) {
            return;
        }

        String username = normalize(tutor.getEmail());
        if (userAccountRepository.existsByUsername(username)) {
            throw new ConflictOperationException("Ja existe usuario cadastrado com este e-mail");
        }

        String code = generateFirstAccessCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        userAccountRepository.save(UserAccount.builder()
                .username(username)
                .password(passwordEncoder.encode(generateTemporaryPassword()))
                .active(false)
                .firstAccessCode(code)
                .firstAccessExpiresAt(expiresAt)
                .role(UserRole.TUTOR)
                .tutor(tutor)
                .build());

        emailService.sendFirstAccessCode(username, tutor.getName(), code, expiresAt);
    }

    private void assertClinicStaff() {
        UserRole role = authenticatedUser.role();
        if (role != UserRole.ADMIN_CLINICA && role != UserRole.VETERINARIO) {
            throw new ForbiddenOperationException("Apenas clínica ou veterinário podem cadastrar tutores");
        }
    }

    private String generateFirstAccessCode() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString();
    }

    private void assertEmailNotChanged(Tutor tutor, String requestedEmail) {
        boolean hasAccessAccount = userAccountRepository.findByTutorId(tutor.getId()).isPresent();
        boolean emailChanged = !Objects.equals(normalize(tutor.getEmail()), normalize(requestedEmail));

        if (hasAccessAccount && emailChanged) {
            throw new ForbiddenOperationException("E-mail do tutor nao pode ser alterado apos a criacao da conta");
        }
        if (!hasAccessAccount && emailChanged) {
            assertUsernameAvailable(requestedEmail);
        }
    }

    private void assertCpfAvailableForCreate(String cpf) {
        if (repository.existsByCpf(cpf)) {
            throw new ConflictOperationException("Ja existe tutor cadastrado com este CPF");
        }
    }

    private void assertCpfAvailableForUpdate(Tutor tutor, String requestedCpf) {
        if (!Objects.equals(tutor.getCpf(), requestedCpf) && repository.existsByCpfAndIdNot(requestedCpf, tutor.getId())) {
            throw new ConflictOperationException("Ja existe tutor cadastrado com este CPF");
        }
    }

    private void assertUsernameAvailable(String email) {
        String username = normalize(email);
        if (username != null && userAccountRepository.existsByUsername(username)) {
            throw new ConflictOperationException("Ja existe usuario cadastrado com este e-mail");
        }
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

