package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Respuesta con el detalle de una promoción.
 */
public record PromotionResponse(
        UUID id,
        String name,
        String type,
        boolean active,
        Instant validFrom,
        Instant validTo,
        BigDecimal percentageValue,
        MoneyResponse fixedAmount,
        List<VolumeTierResponse> volumeTiers,
        int buyQuantity,
        int getQuantity,
        MoneyResponse minPurchaseAmount,
        MoneyResponse maxDiscount,
        int maxUsage,
        int maxUsagePerCustomer,
        int usedCount,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    public record VolumeTierResponse(int minQuantity, String discountType, BigDecimal value) {
    }
}