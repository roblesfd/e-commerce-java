package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una promoción dentro del contexto Promotions &amp; Coupons.
 */
public record PromotionId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public PromotionId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static PromotionId generate() {
        return new PromotionId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link PromotionId} con el valor dado.
     */
    public static PromotionId of(UUID value) {
        return new PromotionId(value);
    }
}