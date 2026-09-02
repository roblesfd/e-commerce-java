package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Petición para ajustar el stock a un valor objetivo.
 */
public record AdjustStockRequest(
        @Min(0) int targetQuantity,
        @Size(max = 200) String reason
) {
}