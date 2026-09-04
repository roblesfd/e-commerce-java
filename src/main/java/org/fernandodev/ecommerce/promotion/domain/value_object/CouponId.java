package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un cupón dentro del contexto Promotions &amp; Coupons.
 */
public record CouponId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public CouponId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static CouponId generate() {
        return new CouponId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link CouponId} con el valor dado.
     */
    public static CouponId of(UUID value) {
        return new CouponId(value);
    }
}