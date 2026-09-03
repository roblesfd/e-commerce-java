package org.fernandodev.ecommerce.returns.adapter.in.rest.dto;

import java.util.Set;
import java.util.UUID;

/**
 * Respuesta de una política de devoluciones.
 */
public record ReturnPolicyResponse(
        UUID id,
        int returnWindowDays,
        boolean active,
        Set<UUID> returnableProductIds
) {
}