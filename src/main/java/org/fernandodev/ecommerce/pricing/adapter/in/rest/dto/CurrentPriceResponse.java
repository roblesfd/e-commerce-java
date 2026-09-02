package org.fernandodev.ecommerce.pricing.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta con el precio vigente de un producto/variante en un instante.
 */
public record CurrentPriceResponse(
        BigDecimal amount,
        String currency,
        Instant at
) {
}