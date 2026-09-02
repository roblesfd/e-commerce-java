package org.fernandodev.ecommerce.cart.adapter.in.rest.dto;

import java.math.BigDecimal;

/**
 * Respuesta con los totales del carrito.
 */
public record TotalsResponse(
        CartResponse.MoneyResponse subtotal,
        CartResponse.MoneyResponse discounts,
        CartResponse.MoneyResponse total
) {

    public static TotalsResponse empty() {
        return new TotalsResponse(null, null, null);
    }
}