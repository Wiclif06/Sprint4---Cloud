package br.com.fiap.petjourney.config;

import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BootstrapAdminTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final Validator validator = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() { FACTORY.close(); }
    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final String password = "test-only-password-2026";

    @Test
    void criaAdministradorComSenhaCriptografada() {
        when(repository.findByUsername("admin@example.test")).thenReturn(Optional.empty());
        when(encoder.encode(password)).thenReturn("hash");
        new BootstrapAdmin(repository, encoder, " Admin@Example.Test ", password, validator).run(null);
        var captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin@example.test");
        assertThat(captor.getValue().getPassword()).isEqualTo("hash");
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN_SISTEMA);
        assertThat(captor.getValue().getActive()).isTrue();
    }

    @Test
    void reinicializacaoPreservaAdministradorESenhaExistentes() {
        when(repository.findByUsername("admin@example.test")).thenReturn(Optional.of(
                UserAccount.builder().role(UserRole.ADMIN_SISTEMA).active(true).build()));
        new BootstrapAdmin(repository, encoder, "admin@example.test", password, validator).run(null);
        verify(repository, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void naoPromoveContaDeOutroPerfil() {
        when(repository.findByUsername("admin@example.test")).thenReturn(Optional.of(
                UserAccount.builder().role(UserRole.TUTOR).active(true).build()));
        assertThatIllegalStateException().isThrownBy(() ->
                new BootstrapAdmin(repository, encoder, "admin@example.test", password, validator).run(null));
        verify(repository, never()).save(any());
    }

    @Test
    void rejeitaSenhaCurtaAntesDeAcessarBanco() {
        assertThatIllegalStateException().isThrownBy(() ->
                new BootstrapAdmin(repository, encoder, "admin@example.test", "short", validator).run(null));
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void naoReativaAdministradorDesativado() {
        when(repository.findByUsername("admin@example.test")).thenReturn(Optional.of(
                UserAccount.builder().role(UserRole.ADMIN_SISTEMA).active(false).build()));
        assertThatIllegalStateException().isThrownBy(() ->
                new BootstrapAdmin(repository, encoder, "admin@example.test", password, validator).run(null));
        verify(repository, never()).save(any());
    }

    @Test
    void respeitaLimiteDeBytesDoBCrypt() {
        assertThatIllegalStateException().isThrownBy(() ->
                new BootstrapAdmin(repository, encoder, "admin@example.test", "á".repeat(40), validator).run(null));
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void rejeitaLoginQueNaoPodeSerAutenticadoPelaApi() {
        assertThatIllegalStateException().isThrownBy(() ->
                new BootstrapAdmin(repository, encoder, "admin-sem-email", password, validator).run(null));
        verifyNoInteractions(repository, encoder);
    }
}
