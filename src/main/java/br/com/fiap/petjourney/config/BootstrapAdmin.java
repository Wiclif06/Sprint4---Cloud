package br.com.fiap.petjourney.config;

import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "petjourney.bootstrap.enabled", havingValue = "true")
public class BootstrapAdmin implements ApplicationRunner {

    private final UserAccountRepository repository;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public BootstrapAdmin(UserAccountRepository repository, PasswordEncoder encoder,
                          @Value("${petjourney.bootstrap.username}") String username,
                          @Value("${petjourney.bootstrap.password}") String password) {
        this.repository = repository;
        this.encoder = encoder;
        this.username = username.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || username.length() > 255 || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Configure ADMIN_USERNAME e ADMIN_PASSWORD (12 a 72 caracteres)");
        }
        var existing = repository.findByUsername(username);
        if (existing.isPresent()) {
            if (existing.get().getRole() != UserRole.ADMIN_SISTEMA || !Boolean.TRUE.equals(existing.get().getActive())) {
                throw new IllegalStateException("O usuario de bootstrap ja existe sem acesso ADMIN_SISTEMA ativo");
            }
            return;
        }
        repository.save(UserAccount.builder()
                .username(username)
                .password(encoder.encode(password))
                .role(UserRole.ADMIN_SISTEMA)
                .active(true)
                .build());
    }
}
