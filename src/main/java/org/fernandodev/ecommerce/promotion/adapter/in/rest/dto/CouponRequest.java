package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear un cupón.
 */
public record CouponRequest(
        @NotBlank @Size(max = 64) String code,
        @NotNull UUID promotionId,
        @NotNull Instant validFrom,
        @NotNull Instant validTo
) {
}