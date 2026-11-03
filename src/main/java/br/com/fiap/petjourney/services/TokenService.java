package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final UserAccountRepository userAccountRepository;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration-minutes}")
    private long expirationMinutes;

    public String generateToken(String username) {
        var normalizedUsername = normalizeUsername(username);
        var user = userAccountRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("role", user.getRole().name());

        Long clinicId = getClinicId(user);
        Long tutorId = getTutorId(user);
        Long veterinarianId = getVeterinarianId(user);
        if (clinicId != null) {
            claims.claim("clinicId", clinicId);
        }
        if (tutorId != null) {
            claims.claim("tutorId", tutorId);
        }
        if (veterinarianId != null) {
            claims.claim("veterinarianId", veterinarianId);
        }

        return encoder.encode(JwtEncoderParameters.from(claims.build())).getTokenValue();
    }

    private Long getClinicId(UserAccount user) {
        if (user.getClinic() != null) {
            return user.getClinic().getId();
        }
        if (user.getTutor() != null && user.getTutor().getClinic() != null) {
            return user.getTutor().getClinic().getId();
        }
        if (user.getVeterinarian() != null && user.getVeterinarian().getClinic() != null) {
            return user.getVeterinarian().getClinic().getId();
        }
        return null;
    }

    private Long getTutorId(UserAccount user) {
        return user.getTutor() != null ? user.getTutor().getId() : null;
    }

    private Long getVeterinarianId(UserAccount user) {
        return user.getVeterinarian() != null ? user.getVeterinarian().getId() : null;
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}

