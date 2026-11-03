package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var normalizedUsername = normalizeUsername(username);
        var user = userAccountRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .roles(user.getRole().name())
                .build();
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}

