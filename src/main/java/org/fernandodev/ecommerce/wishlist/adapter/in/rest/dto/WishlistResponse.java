package org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta de una wishlist.
 */
public record WishlistResponse(
        UUID id,
        UUID customerId,
        String name,
        List<ItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {

    public record ItemResponse(UUID productId, UUID variantId, Instant addedAt) {
    }
}