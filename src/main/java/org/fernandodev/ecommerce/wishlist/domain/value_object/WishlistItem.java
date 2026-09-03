package org.fernandodev.ecommerce.wishlist.domain.value_object;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Item de una wishlist. Referencia identificadores de catálogo sin duplicar
 * información del producto.
 *
 * @param productId producto guardado.
 * @param variantId variante (null si el item es a nivel producto).
 * @param addedAt   fecha de alta del item.
 */
public record WishlistItem(ProductId productId, ProductVariantId variantId, Instant addedAt) {

    public WishlistItem {
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(addedAt, "addedAt no puede ser null");
    }
}