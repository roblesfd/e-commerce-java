package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para disminuir el stock.
 */
public record DecreaseStockRequest(
        @Positive int quantity,
        @Size(max = 200) String reason
) {
}