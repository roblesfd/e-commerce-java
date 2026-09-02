package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un cupón dentro del contexto Promotions & Coupons.
 */
public record CouponId(UUID value) {

    public CouponId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static CouponId generate() {
        return new CouponId(UUID.randomUUID());
    }

    public static CouponId of(UUID value) {
        return new CouponId(value);
    }
}