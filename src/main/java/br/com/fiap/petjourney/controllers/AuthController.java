package br.com.fiap.petjourney.controllers;

import br.com.fiap.petjourney.dtos.request.LoginRequest;
import br.com.fiap.petjourney.dtos.request.FirstAccessRequest;
import br.com.fiap.petjourney.dtos.response.LoginResponse;
import br.com.fiap.petjourney.dtos.response.UserAccountResponse;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import br.com.fiap.petjourney.services.AuthenticatedUserService;
import br.com.fiap.petjourney.services.FirstAccessService;
import br.com.fiap.petjourney.services.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatedUserService authenticatedUser;
    private final FirstAccessService firstAccessService;

    @PostMapping({"/login", "/auth/login"})
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        String username = normalizeUsername(request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password())
        );

        var user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return new LoginResponse(
                tokenService.generateToken(user.getUsername()),
                user.getRole(),
                UserAccountResponse.fromEntity(user).clinicId(),
                user.getTutor() != null ? user.getTutor().getId() : null,
                user.getVeterinarian() != null ? user.getVeterinarian().getId() : null
        );
    }

    @GetMapping("/auth/me")
    public UserAccountResponse me() {
        var user = userAccountRepository.findByUsername(authenticatedUser.username())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return UserAccountResponse.fromEntity(user);
    }

    @PostMapping("/auth/first-access/activate")
    public UserAccountResponse activateFirstAccess(@RequestBody @Valid FirstAccessRequest request) {
        return firstAccessService.activate(request);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}

