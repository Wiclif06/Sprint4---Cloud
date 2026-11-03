package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.FirstAccessRequest;
import br.com.fiap.petjourney.dtos.response.UserAccountResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FirstAccessService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountResponse activate(FirstAccessRequest request) {
        var user = userAccountRepository.findByUsernameAndFirstAccessCode(normalizeUsername(request.username()), request.code())
                .orElseThrow(() -> new ForbiddenOperationException("Código de primeiro acesso inválido"));

        if (Boolean.TRUE.equals(user.getActive())) {
            throw new ForbiddenOperationException("Conta já ativada");
        }
        if (user.getFirstAccessUsedAt() != null) {
            throw new ForbiddenOperationException("Código de primeiro acesso já utilizado");
        }
        if (user.getFirstAccessExpiresAt() == null || user.getFirstAccessExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ForbiddenOperationException("Código de primeiro acesso expirado");
        }

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setFirstAccessUsedAt(LocalDateTime.now());
        user.setFirstAccessCode(null);
        user.setFirstAccessExpiresAt(null);

        return UserAccountResponse.fromEntity(userAccountRepository.save(user));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}

