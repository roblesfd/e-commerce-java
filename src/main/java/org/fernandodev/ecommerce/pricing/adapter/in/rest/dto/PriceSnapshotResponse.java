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

    /**
     * Importe monetario en la instantánea.
     *
     * @param amount   cantidad.
     * @param currency divisa ISO 4217.
     */
    public record Money(BigDecimal amount, String currency) {
    }
}