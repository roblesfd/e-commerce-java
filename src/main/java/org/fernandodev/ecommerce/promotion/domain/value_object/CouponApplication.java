package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.Objects;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Resultado de aplicar un cupón a un carrito.
 *
 * @param code        código del cupón aplicado.
 * @param promotionId promoción subyacente.
 * @param discount    descuento calculado.
 */
public record CouponApplication(String code, PromotionId promotionId, Money discount) {

    public CouponApplication {
        Objects.requireNonNull(code, "code no puede ser null");
        Objects.requireNonNull(promotionId, "promotionId no puede ser null");
        Objects.requireNonNull(discount, "discount no puede ser null");
    }
}