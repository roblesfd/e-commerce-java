package org.fernandodev.ecommerce.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Principal de autenticación del usuario IAM.
 * <p>
 * Construido a partir de la validación remota del token contra el IAM
 * (identidad, nombre de usuario y permisos efectivos). Roles y permisos NO se
 * gestionan en E-Commerce; se obtienen del IAM en cada validación.
 * </p>
 *
 * @param id       identificador del usuario IAM.
 * @param username nombre de usuario.
 * @param permisos permisos efectivos del usuario.
 */
public record AuthenticatedUser(UUID id, String username, Set<String> permisos) {

    public static AuthenticatedUser of(UUID id, String username, Set<String> permisos) {
        return new AuthenticatedUser(id, username, permisos == null ? Set.of() : Set.copyOf(permisos));
    }
}