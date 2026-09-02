package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Tramo de un descuento por volumen: a partir de una cantidad mínima se aplica
 * un descuento (porcentaje o monto fijo).
 *
 * @param minQuantity  cantidad mínima para activar el tramo.
 * @param discountType tipo de descuento del tramo.
 * @param value        valor del descuento (porcentaje o monto).
 */
public record VolumeTier(int minQuantity, DiscountType discountType, BigDecimal value) {

    public VolumeTier {
        Objects.requireNonNull(discountType, "discountType no puede ser null");
        Objects.requireNonNull(value, "value no puede ser null");
        if (minQuantity <= 0) {
            throw new IllegalArgumentException("minQuantity debe ser positivo");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("value debe ser positivo");
        }
    }
}