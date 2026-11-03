package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.VeterinarianRequest;
import br.com.fiap.petjourney.dtos.response.VeterinarianResponse;
import br.com.fiap.petjourney.exceptions.ConflictOperationException;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Clinic;
import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.Veterinarian;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.ClinicRepository;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import br.com.fiap.petjourney.repositories.VeterinarianRepository;
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
public class VeterinarianService {

    private final VeterinarianRepository repository;
    private final ClinicRepository clinicRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticatedUserService authenticatedUser;
    private final SecureRandom secureRandom = new SecureRandom();

    public Page<VeterinarianResponse> findAll(String name, Pageable pageable) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            Long clinicId = authenticatedUser.clinicId();
            if (name != null && !name.isBlank()) {
                return repository.findByClinicIdAndActiveTrueAndNameContainingIgnoreCase(clinicId, name, pageable).map(VeterinarianResponse::fromEntity);
            }
            return repository.findByClinicIdAndActiveTrue(clinicId, pageable).map(VeterinarianResponse::fromEntity);
        }

        if (name != null && !name.isBlank()) {
            return repository.findByActiveTrueAndNameContainingIgnoreCase(name, pageable).map(VeterinarianResponse::fromEntity);
        }

        return repository.findByActiveTrue(pageable).map(VeterinarianResponse::fromEntity);
    }

    public VeterinarianResponse findById(Long id) {
        return VeterinarianResponse.fromEntity(findAccessibleVeterinarian(id));
    }

    @Transactional
    public VeterinarianResponse create(VeterinarianRequest request) {
        assertAdminClinic();
        Long clinicId = authenticatedUser.clinicId();
        if (!request.clinicId().equals(clinicId)) {
            throw new ForbiddenOperationException("Administrador nao pode cadastrar veterinario em outra clinica");
        }
        assertUsernameAvailable(request.email());

        Clinic clinic = clinicRepository.findByIdAndActiveTrue(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinica nao encontrada"));

        Veterinarian veterinarian = repository.save(new Veterinarian(request, clinic));
        createInactiveVeterinarianAccess(veterinarian);

        return VeterinarianResponse.fromEntity(veterinarian);
    }

    @Transactional
    public VeterinarianResponse update(Long id, VeterinarianRequest request) {
        assertAdminClinic();
        Veterinarian veterinarian = findAccessibleVeterinarian(id);
        assertEmailNotChanged(veterinarian, request.email());

        if (!request.clinicId().equals(authenticatedUser.clinicId())) {
            throw new ForbiddenOperationException("Administrador nao pode mover veterinario para outra clinica");
        }

        Clinic clinic = clinicRepository.findByIdAndActiveTrue(authenticatedUser.clinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinica nao encontrada"));

        veterinarian.updateFrom(request, clinic);

        return VeterinarianResponse.fromEntity(repository.save(veterinarian));
    }

    @Transactional
    public void delete(Long id) {
        assertAdminClinic();
        Veterinarian veterinarian = findAccessibleVeterinarian(id);
        userAccountRepository.deactivateVeterinarianAccount(veterinarian.getId());
        veterinarian.setActive(false);
        repository.save(veterinarian);
    }

    public Veterinarian findAccessibleVeterinarian(Long id) {
        UserRole role = authenticatedUser.role();
        if (role == UserRole.ADMIN_CLINICA || role == UserRole.VETERINARIO) {
            return repository.findByIdAndClinicIdAndActiveTrue(id, authenticatedUser.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado para esta clinica"));
        }
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado"));
    }

    private void assertAdminClinic() {
        if (authenticatedUser.role() != UserRole.ADMIN_CLINICA) {
            throw new ForbiddenOperationException("Apenas administradores de clinica podem alterar veterinarios");
        }
    }

    private void createInactiveVeterinarianAccess(Veterinarian veterinarian) {
        if (veterinarian.getEmail() == null || veterinarian.getEmail().isBlank()) {
            return;
        }

        String username = normalize(veterinarian.getEmail());
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
                .role(UserRole.VETERINARIO)
                .veterinarian(veterinarian)
                .build());

        emailService.sendFirstAccessCode(username, veterinarian.getName(), code, expiresAt);
    }

    private void assertEmailNotChanged(Veterinarian veterinarian, String requestedEmail) {
        boolean hasAccessAccount = userAccountRepository.findByVeterinarianId(veterinarian.getId()).isPresent();
        boolean emailChanged = !Objects.equals(normalize(veterinarian.getEmail()), normalize(requestedEmail));

        if (hasAccessAccount && emailChanged) {
            throw new ForbiddenOperationException("E-mail do veterinario nao pode ser alterado apos a criacao da conta");
        }
        if (!hasAccessAccount && emailChanged) {
            assertUsernameAvailable(requestedEmail);
        }
    }

    private void assertUsernameAvailable(String email) {
        String username = normalize(email);
        if (username != null && userAccountRepository.existsByUsername(username)) {
            throw new ConflictOperationException("Ja existe usuario cadastrado com este e-mail");
        }
    }

    private String generateFirstAccessCode() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString();
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

