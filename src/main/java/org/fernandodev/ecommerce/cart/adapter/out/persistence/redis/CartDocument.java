package org.fernandodev.ecommerce.cart.adapter.out.persistence.redis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representación serializable del carrito para persistir en Redis (JSON).
 */
public record CartDocument(
        UUID id,
        UUID ownerId,
        Instant updatedAt,
        List<Item> items,
        Coupon coupon
) {

    public record Item(
            UUID id,
            UUID productId,
            UUID variantId,
            int quantity,
            BigDecimal amount,
            String currency
    ) {
    }

    public record Coupon(String code, BigDecimal amount, String currency) {
    }
}