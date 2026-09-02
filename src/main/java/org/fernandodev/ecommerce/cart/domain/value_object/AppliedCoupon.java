package org.fernandodev.ecommerce.cart.domain.value_object;

import java.util.Objects;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Cupón aplicado a un carrito.
 *
 * @param code     código del cupón aplicado.
 * @param discount descuento calculado por el contexto Promotions.
 */
public record AppliedCoupon(String code, Money discount) {

    public AppliedCoupon {
        Objects.requireNonNull(code, "code no puede ser null");
        Objects.requireNonNull(discount, "discount no puede ser null");
    }
}