package org.fernandodev.ecommerce.pricing.application.dto;

import java.math.BigDecimal;

/**
 * Datos para actualizar el precio base.
 *
 * @param amount   nuevo importe.
 * @param currency divisa ISO 4217.
 */
public record UpdatePriceCommand(BigDecimal amount, String currency) {
}