package org.fernandodev.ecommerce.promotion.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.VolumeTier;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Datos para crear una promoción.
 */
public record CreatePromotionCommand(
        String name,
        DiscountType type,
        BigDecimal percentageValue,
        Money fixedAmount,
        List<VolumeTier> volumeTiers,
        int buyQuantity,
        int getQuantity,
        Instant validFrom,
        Instant validTo,
        Money minPurchaseAmount,
        Money maxDiscount,
        int maxUsage,
        int maxUsagePerCustomer,
        Set<ProductId> productIds,
        Set<CategoryId> categoryIds
) {

    public CreatePromotionCommand {
        volumeTiers = volumeTiers == null ? List.of() : volumeTiers;
        productIds = productIds == null ? Set.of() : productIds;
        categoryIds = categoryIds == null ? Set.of() : categoryIds;
    }
}