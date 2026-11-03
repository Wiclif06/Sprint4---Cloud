package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.CreateClinicAdminRequest;
import br.com.fiap.petjourney.dtos.response.UserAccountResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.ClinicRepository;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemAdminService {

    private final ClinicRepository clinicRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatedUserService authenticatedUser;
    private final PasswordEncoder passwordEncoder;

    public UserAccountResponse createClinicAdmin(Long clinicId, CreateClinicAdminRequest request) {
        if (authenticatedUser.role() != UserRole.ADMIN_SISTEMA) {
            throw new ForbiddenOperationException("Apenas administradores do sistema podem criar administradores de clínica");
        }

        String username = normalizeUsername(request.username());

        if (userAccountRepository.existsByUsername(username)) {
            throw new ForbiddenOperationException("Já existe usuário cadastrado com este e-mail");
        }

        var clinic = clinicRepository.findByIdAndActiveTrue(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada"));

        var user = UserAccount.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ADMIN_CLINICA)
                .clinic(clinic)
                .build();

        return UserAccountResponse.fromEntity(userAccountRepository.save(user));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}

