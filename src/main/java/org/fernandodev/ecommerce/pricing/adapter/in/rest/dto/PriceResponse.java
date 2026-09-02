package org.fernandodev.ecommerce.pricing.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el detalle de un precio.
 */
public record PriceResponse(
        UUID id,
        UUID productId,
        UUID variantId,
        MoneyResponse basePrice,
        List<PriceChangeResponse> history,
        List<PriceOverrideResponse> overrides
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    public record PriceChangeResponse(BigDecimal amount, String currency, Instant effectiveAt) {
    }

    public record PriceOverrideResponse(
            UUID id,
            BigDecimal amount,
            String currency,
            Instant validFrom,
            Instant validTo,
            String type
    ) {
    }
}