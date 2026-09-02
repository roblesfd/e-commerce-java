package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Petición para reservar stock.
 */
public record ReserveStockRequest(
        @Positive int quantity,
        @NotNull Instant expiresAt
) {
}