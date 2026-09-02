package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import java.math.BigDecimal;

import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con un tramo de descuento por volumen.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class VolumeTierEmbeddable {

    @Column(name = "min_quantity", nullable = false)
    private int minQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "value", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;
}