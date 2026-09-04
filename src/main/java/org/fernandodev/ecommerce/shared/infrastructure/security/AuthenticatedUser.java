package org.fernandodev.ecommerce.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Representa al usuario autenticado dentro del contexto de seguridad
 * de E-Commerce.
 *
 * <p>El principal se construye a partir de la información obtenida
 * mediante la validación remota del token contra el IAM. Contiene la
 * identidad del usuario y sus permisos, roles y grupos efectivos.</p>
 *
 * <p>E-Commerce no administra directamente los roles ni permisos del
 * usuario. Esta información es proporcionada por el IAM como parte de
 * la validación del token.</p>
 *
 * <p>Las colecciones recibidas durante la construcción se copian de forma
 * defensiva mediante {@link Set#copyOf(java.util.Collection)}, evitando
 * que modificaciones posteriores de las colecciones originales puedan
 * alterar el estado del principal.</p>
 *
 * @param id identificador único del usuario en el IAM.
 * @param username nombre de usuario asociado a la identidad autenticada.
 * @param permisos conjunto de permisos efectivos del usuario.
 * @param roles conjunto de roles efectivos del usuario.
 * @param grupos conjunto de grupos a los que pertenece el usuario.
 */
public record AuthenticatedUser(
        UUID id,
        String username,
        Set<String> permisos,
        Set<String> roles,
        Set<String> grupos) {

    /**
     * Crea una instancia de {@code AuthenticatedUser} realizando una
     * copia defensiva de las colecciones de permisos, roles y grupos.
     *
     * <p>Las colecciones {@code null} se convierten en conjuntos vacíos,
     * evitando la propagación de valores {@code null} dentro del principal
     * autenticado.</p>
     *
     * @param id identificador único del usuario en el IAM.
     * @param username nombre de usuario.
     * @param permisos permisos efectivos del usuario.
     * @param roles roles efectivos del usuario.
     * @param grupos grupos del usuario.
     * @return una nueva instancia de {@code AuthenticatedUser} con las
     *         colecciones copiadas de forma defensiva.
     */
    public static AuthenticatedUser of(
            UUID id,
            String username,
            Set<String> permisos,
            Set<String> roles,
            Set<String> grupos) {

        return new AuthenticatedUser(
                id,
                username,
                copy(permisos),
                copy(roles),
                copy(grupos)
        );
    }

    /**
     * Crea una copia inmutable del conjunto proporcionado.
     *
     * <p>Si el conjunto recibido es {@code null}, devuelve
     * {@link Set#of()} en lugar de propagar un valor {@code null}.</p>
     *
     * @param values conjunto cuyos elementos serán copiados.
     * @return conjunto inmutable con los mismos elementos o un conjunto
     *         vacío si {@code values} es {@code null}.
     */
    private static Set<String> copy(Set<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }
}
