package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para incrementar el stock.
 */
public record IncreaseStockRequest(
        @Positive int quantity,
        @Size(max = 200) String reason
) {
}