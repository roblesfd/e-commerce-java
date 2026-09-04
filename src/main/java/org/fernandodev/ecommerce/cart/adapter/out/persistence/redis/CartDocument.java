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

    /**
     * Item del carrito serializable.
     *
     * @param id        identificador del item.
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad.
     * @param amount    precio unitario.
     * @param currency  divisa ISO 4217.
     */
    public record Item(
            UUID id,
            UUID productId,
            UUID variantId,
            int quantity,
            BigDecimal amount,
            String currency
    ) {
    }

    /**
     * Cupón aplicado serializable.
     *
     * @param code     código del cupón.
     * @param amount   descuento.
     * @param currency divisa ISO 4217.
     */
    public record Coupon(String code, BigDecimal amount, String currency) {
    }
}