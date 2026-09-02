package org.fernandodev.ecommerce.promotion.application.dto;

import java.time.Instant;

/**
 * Datos para actualizar la validez de un cupón.
 */
public record UpdateCouponCommand(Instant validFrom, Instant validTo) {
}