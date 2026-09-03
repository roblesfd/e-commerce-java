package org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Petición para añadir un item a una wishlist.
 */
public record AddItemRequest(
        @NotNull UUID productId,
        UUID variantId
) {
}