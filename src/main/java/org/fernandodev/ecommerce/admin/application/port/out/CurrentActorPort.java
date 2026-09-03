package org.fernandodev.ecommerce.admin.application.port.out;

import java.util.UUID;

/**
 * Puerto hacia la autenticación actual para registrar el actor en auditoría.
 */
public interface CurrentActorPort {

    UUID actorId();
}