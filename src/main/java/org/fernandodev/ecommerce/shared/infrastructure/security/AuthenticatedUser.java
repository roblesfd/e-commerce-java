package org.fernandodev.ecommerce.shared.infrastructure.security;

import java.util.UUID;

/**
 * Principal de autenticación del usuario IAM.
 * <p>
 * Contiene únicamente el identificador del usuario en la plataforma IAM.
 * Roles y permisos NO se replican en E-Commerce (pertenecen al IAM); la
 * autorización se basa en la propiedad de los recursos del propio usuario.
 * </p>
 *
 * @param id identificador del usuario IAM.
 */
public record AuthenticatedUser(UUID id) {
}