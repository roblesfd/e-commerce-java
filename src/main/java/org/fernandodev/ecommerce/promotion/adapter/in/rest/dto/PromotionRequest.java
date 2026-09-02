package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear una promoción.
 */
public record PromotionRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull DiscountType type,
        BigDecimal percentageValue,
        MoneyRequest fixedAmount,
        List<@Valid VolumeTierRequest> volumeTiers,
        Integer buyQuantity,
        Integer getQuantity,
        @NotNull Instant validFrom,
        @NotNull Instant validTo,
        MoneyRequest minPurchaseAmount,
        MoneyRequest maxDiscount,
        int maxUsage,
        int maxUsagePerCustomer,
        Set<UUID> productIds,
        Set<UUID> categoryIds
) {

    public PromotionRequest {
        volumeTiers = volumeTiers == null ? List.of() : volumeTiers;
        productIds = productIds == null ? Set.of() : productIds;
        categoryIds = categoryIds == null ? Set.of() : categoryIds;
    }

    public record MoneyRequest(@NotNull BigDecimal amount, @NotBlank @Size(min = 3, max = 3) String currency) {
    }

    public record VolumeTierRequest(
            @Positive int minQuantity,
            @NotNull DiscountType discountType,
            @NotNull BigDecimal value
    ) {
    }
}