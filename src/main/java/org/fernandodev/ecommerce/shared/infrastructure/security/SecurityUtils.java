package org.fernandodev.ecommerce.shared.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utilidades de acceso al principal autenticado.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Devuelve el identificador del usuario IAM autenticado, si existe.
     *
     * @return el identificador del usuario IAM o vacío si no hay sesión.
     */
    public static Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user.id());
        }
        return Optional.empty();
    }

    /**
     * Devuelve el identificador del usuario IAM autenticado o lanza una
     * excepción si no hay sesión.
     *
     * @return el identificador del usuario IAM.
     * @throws IllegalStateException si no hay usuario autenticado.
     */
    public static UUID requiredUserId() {
        return currentUserId()
                .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));
    }
}