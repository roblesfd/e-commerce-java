package org.fernandodev.ecommerce.reviews.application.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

/**
 * Resumen de rating de un producto: total, promedio y distribución por rating.
 */
public record RatingSummary(long count, BigDecimal average, Map<Integer, Long> distribution) {

    public static RatingSummary empty() {
        return new RatingSummary(0, BigDecimal.ZERO.setScale(2), new TreeMap<>());
    }

    public static RatingSummary of(long count, BigDecimal average, Map<Integer, Long> distribution) {
        return new RatingSummary(count, average.setScale(2, RoundingMode.HALF_UP), new TreeMap<>(distribution));
    }
}