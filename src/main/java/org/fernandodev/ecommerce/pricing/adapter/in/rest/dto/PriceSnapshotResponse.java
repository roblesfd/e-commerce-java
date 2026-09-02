package org.fernandodev.ecommerce.pricing.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta con la instantánea de precio lista para capturar en una orden.
 */
public record PriceSnapshotResponse(
        UUID productId,
        UUID variantId,
        Money amount,
        Money basePrice,
        Money discount,
        Instant at
) {

    public record Money(BigDecimal amount, String currency) {
    }
}