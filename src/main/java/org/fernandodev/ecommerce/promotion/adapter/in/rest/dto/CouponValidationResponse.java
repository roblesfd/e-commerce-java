package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.math.BigDecimal;

/**
 * Respuesta con la validación de un cupón.
 */
public record CouponValidationResponse(
        boolean valid,
        String reason,
        MoneyResponse discount
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }
}