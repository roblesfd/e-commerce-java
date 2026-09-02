package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una promoción dentro del contexto Promotions & Coupons.
 */
public record PromotionId(UUID value) {

    public PromotionId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static PromotionId generate() {
        return new PromotionId(UUID.randomUUID());
    }

    public static PromotionId of(UUID value) {
        return new PromotionId(value);
    }
}