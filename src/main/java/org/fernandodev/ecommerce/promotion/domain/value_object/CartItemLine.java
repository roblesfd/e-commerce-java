package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Línea del carrito evaluada por una promoción.
 *
 * @param productId    producto.
 * @param categoryIds  categorías del producto (resueltas desde catalog).
 * @param quantity     cantidad.
 * @param unitPrice    precio unitario.
 */
public record CartItemLine(
        ProductId productId,
        Set<CategoryId> categoryIds,
        int quantity,
        Money unitPrice
) {

    public CartItemLine {
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(unitPrice, "unitPrice no puede ser null");
        categoryIds = categoryIds == null ? Set.of() : Set.copyOf(categoryIds);
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity debe ser positivo");
        }
    }

    public Money lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}