package org.fernandodev.ecommerce.returns.adapter.in.rest.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Positive;

/**
 * Petición para crear una política de devoluciones.
 */
public record CreateReturnPolicyRequest(
        @Positive int returnWindowDays,
        Set<UUID> returnableProductIds
) {
}