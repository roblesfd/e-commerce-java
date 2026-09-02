package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

import jakarta.validation.constraints.Min;

/**
 * Petición para definir el nivel mínimo de stock.
 */
public record SetMinimumStockRequest(
        @Min(0) int minimumStock
) {
}