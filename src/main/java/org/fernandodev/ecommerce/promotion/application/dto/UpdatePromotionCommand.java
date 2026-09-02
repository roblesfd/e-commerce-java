package org.fernandodev.ecommerce.promotion.application.dto;

import java.time.Instant;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Datos para actualizar una promoción (nombre, validez, límites y scope).
 */
public record UpdatePromotionCommand(
        String name,
        Instant validFrom,
        Instant validTo,
        Money minPurchaseAmount,
        Money maxDiscount,
        int maxUsage,
        int maxUsagePerCustomer,
        Set<ProductId> productIds,
        Set<CategoryId> categoryIds
) {

    public UpdatePromotionCommand {
        productIds = productIds == null ? Set.of() : productIds;
        categoryIds = categoryIds == null ? Set.of() : categoryIds;
    }
}