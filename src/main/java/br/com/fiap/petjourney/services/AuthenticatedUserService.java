package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.models.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    public String username() {
        return jwt().getSubject();
    }

    public UserRole role() {
        return UserRole.valueOf(jwt().getClaimAsString("role"));
    }

    public Long clinicId() {
        return getLongClaim("clinicId");
    }

    public Long tutorId() {
        return getLongClaim("tutorId");
    }

    public Long veterinarianId() {
        return getLongClaim("veterinarianId");
    }

    private Long getLongClaim(String name) {
        Object value = jwt().getClaims().get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    private Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt token)) {
            throw new IllegalStateException("Usuário autenticado não encontrado");
        }
        return token;
    }
}

