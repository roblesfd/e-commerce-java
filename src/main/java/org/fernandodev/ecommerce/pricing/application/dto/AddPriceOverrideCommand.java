package org.fernandodev.ecommerce.pricing.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import org.fernandodev.ecommerce.pricing.domain.value_object.PriceType;

/**
 * Datos para añadir un override (promocional o por tiempo).
 *
 * @param amount     importe aplicable en la ventana.
 * @param currency   divisa ISO 4217.
 * @param validFrom  inicio de la ventana (inclusive).
 * @param validTo    fin de la ventana (exclusive).
 * @param type       tipo de override.
 */
public record AddPriceOverrideCommand(
        BigDecimal amount,
        String currency,
        Instant validFrom,
        Instant validTo,
        PriceType type
) {
}