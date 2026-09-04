package org.fernandodev.ecommerce.cart.domain.value_object;

import java.math.BigDecimal;
import java.util.Objects;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Item del carrito: referencia a un producto o variante con su cantidad y el
 * precio unitario capturado al añadirlo (snapshot del contexto Pricing).
 *
 * @param id        identificador del item.
 * @param productId producto.
 * @param variantId variante (null si el item es a nivel producto).
 * @param quantity  cantidad solicitada.
 * @param unitPrice precio unitario capturado.
 */
public record CartItem(
        CartItemId id,
        ProductId productId,
        ProductVariantId variantId,
        int quantity,
        Money unitPrice
) {

    /**
     * Constructor compacto que valida los invariantes del item.
     *
     * @throws NullPointerException     si {@code id}, {@code productId} o {@code unitPrice} son {@code null}.
     * @throws IllegalArgumentException si {@code quantity} no es positiva.
     */
    public CartItem {
        Objects.requireNonNull(id, "id no puede ser null");
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(unitPrice, "unitPrice no puede ser null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity debe ser positivo");
        }
    }

    /**
     * @return el subtotal del item ({@code unitPrice × quantity}).
     */
    public Money subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}