package org.fernandodev.ecommerce.returns.domain.value_object;

import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Item devuelto de una orden, con su monto de reembolso calculado.
 *
 * @param orderItemId  item de la orden original.
 * @param productId    producto devuelto.
 * @param quantity     cantidad devuelta.
 * @param refundAmount monto a reembolsar por este item.
 */
public record ReturnItem(UUID orderItemId, ProductId productId, int quantity, Money refundAmount) {

    public ReturnItem {
        Objects.requireNonNull(orderItemId, "orderItemId no puede ser null");
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(refundAmount, "refundAmount no puede ser null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity debe ser positivo");
        }
    }
}