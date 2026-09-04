package org.fernandodev.ecommerce.checkout.domain.value_object;

import java.math.BigDecimal;
import java.util.Objects;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Item del checkout con el precio vigente capturado en la validación.
 *
 * @param productId producto.
 * @param variantId variante (null si el item es a nivel producto).
 * @param quantity  cantidad.
 * @param unitPrice precio unitario vigente.
 */
public record CheckoutItem(
        ProductId productId,
        ProductVariantId variantId,
        int quantity,
        Money unitPrice
) {

    /**
     * Constructor compacto que valida los invariantes del item.
     *
     * @throws NullPointerException     si {@code productId} o {@code unitPrice} son {@code null}.
     * @throws IllegalArgumentException si {@code quantity} no es positiva.
     */
    public CheckoutItem {
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