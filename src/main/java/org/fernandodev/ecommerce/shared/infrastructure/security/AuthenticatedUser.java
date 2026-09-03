package org.fernandodev.ecommerce.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Principal de autenticación del usuario IAM.
 * <p>
 * Construido a partir de la validación remota del token contra el IAM
 * (identidad, nombre de usuario, permisos, roles y grupos efectivos). Roles y
 * permisos NO se gestionan en E-Commerce; se obtienen del IAM en cada
 * validación.
 * </p>
 *
 * @param id       identificador del usuario IAM.
 * @param username nombre de usuario.
 * @param permisos permisos efectivos del usuario.
 * @param roles    nombres de los roles efectivos del usuario.
 * @param grupos   nombres de los grupos del usuario.
 */
public record AuthenticatedUser(UUID id, String username, Set<String> permisos,
                                Set<String> roles, Set<String> grupos) {

    public static AuthenticatedUser of(UUID id, String username, Set<String> permisos,
                                       Set<String> roles, Set<String> grupos) {
        return new AuthenticatedUser(id, username, copy(permisos), copy(roles), copy(grupos));
    }

    private static Set<String> copy(Set<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }
}