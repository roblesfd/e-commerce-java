package org.fernandodev.ecommerce.admin.adapter.out.adapters;

import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.out.CurrentActorPort;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Component;

/**
 * Adaptador del puerto {@link CurrentActorPort} hacia el usuario autenticado.
 */
@Component
public class SecurityCurrentActor implements CurrentActorPort {

    @Override
    public UUID actorId() {
        return SecurityUtils.requiredUserId();
    }
}