package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.List;
import java.util.Objects;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Contexto del carrito que evalúan las promociones (líneas con producto,
 * categorías, cantidad y precio).
 */
public record CartContext(List<CartItemLine> items) {

    public CartContext {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public Money subtotal() {
        return items.stream()
                .map(CartItemLine::lineTotal)
                .reduce(Money::add)
                .orElseThrow(() -> new IllegalStateException("El carrito no puede estar vacío"));
    }
}