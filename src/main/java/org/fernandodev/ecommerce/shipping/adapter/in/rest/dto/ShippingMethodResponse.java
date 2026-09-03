package org.fernandodev.ecommerce.shipping.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta con el detalle de un método de envío.
 */
public record ShippingMethodResponse(
        UUID id,
        String name,
        String carrier,
        MoneyResponse cost,
        boolean active
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }
}