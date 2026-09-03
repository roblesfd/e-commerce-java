package org.fernandodev.ecommerce.reviews.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Respuesta del resumen de rating de un producto.
 */
public record RatingSummaryResponse(long count, BigDecimal average, Map<Integer, Long> distribution) {
}