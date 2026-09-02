package org.fernandodev.ecommerce.cart.adapter.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Petición para añadir un item al carrito.
 */
public record AddItemRequest(
        @NotNull UUID productId,
        UUID variantId,
        @Positive int quantity
) {
}