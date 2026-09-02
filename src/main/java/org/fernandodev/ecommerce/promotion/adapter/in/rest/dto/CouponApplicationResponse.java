package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta con la aplicación de un cupón.
 */
public record CouponApplicationResponse(
        String code,
        UUID promotionId,
        MoneyResponse discount
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }
}