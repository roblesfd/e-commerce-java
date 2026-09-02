package org.fernandodev.ecommerce.pricing.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

import org.fernandodev.ecommerce.pricing.domain.value_object.PriceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para añadir un override (promocional o por tiempo).
 */
public record AddOverrideRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull Instant validFrom,
        @NotNull Instant validTo,
        @NotNull PriceType type
) {
}