package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta con el detalle de un cupón.
 */
public record CouponResponse(
        UUID id,
        String code,
        UUID promotionId,
        boolean active,
        Instant validFrom,
        Instant validTo
) {
}