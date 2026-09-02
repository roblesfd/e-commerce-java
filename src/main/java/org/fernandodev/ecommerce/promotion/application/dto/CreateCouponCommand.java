package org.fernandodev.ecommerce.promotion.application.dto;

import java.time.Instant;

import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;

/**
 * Datos para crear un cupón.
 */
public record CreateCouponCommand(
        String code,
        PromotionId promotionId,
        Instant validFrom,
        Instant validTo
) {
}