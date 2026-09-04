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

    /**
     * Importe monetario en la respuesta.
     *
     * @param amount   cantidad.
     * @param currency divisa ISO 4217.
     */
    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    /**
     * Cambio de precio base en la respuesta (historial).
     *
     * @param amount       importe del valor anterior.
     * @param currency     divisa ISO 4217.
     * @param effectiveAt  momento del cambio.
     */
    public record PriceChangeResponse(BigDecimal amount, String currency, Instant effectiveAt) {
    }

    /**
     * Override de precio en la respuesta.
     *
     * @param id        identificador del override.
     * @param amount    importe aplicable.
     * @param currency  divisa ISO 4217.
     * @param validFrom inicio de la ventana.
     * @param validTo   fin de la ventana.
     * @param type      tipo de override.
     */
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