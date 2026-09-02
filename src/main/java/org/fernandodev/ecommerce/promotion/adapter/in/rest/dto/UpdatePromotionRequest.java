package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Petición para actualizar una promoción.
 */
public record UpdatePromotionRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Instant validFrom,
        @NotNull Instant validTo,
        PromotionRequest.MoneyRequest minPurchaseAmount,
        PromotionRequest.MoneyRequest maxDiscount,
        int maxUsage,
        int maxUsagePerCustomer,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {

    public UpdatePromotionRequest {
        productIds = productIds == null ? Set.of() : productIds;
        categoryIds = categoryIds == null ? Set.of() : categoryIds;
    }
}