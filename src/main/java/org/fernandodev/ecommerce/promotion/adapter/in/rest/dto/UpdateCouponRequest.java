package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

/**
 * Petición para actualizar la validez de un cupón.
 */
public record UpdateCouponRequest(
        @NotNull Instant validFrom,
        @NotNull Instant validTo
) {
}