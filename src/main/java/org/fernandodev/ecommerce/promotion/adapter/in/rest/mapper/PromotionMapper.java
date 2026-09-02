package org.fernandodev.ecommerce.promotion.adapter.in.rest.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.ApplyCouponRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.PromotionRequest;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.PromotionResponse;
import org.fernandodev.ecommerce.promotion.adapter.in.rest.dto.UpdatePromotionRequest;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdatePromotionCommand;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.VolumeTier;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio de promociones.
 */
@Component
public class PromotionMapper {

    public CreatePromotionCommand toCreateCommand(PromotionRequest request) {
        return new CreatePromotionCommand(
                request.name(),
                request.type(),
                request.percentageValue(),
                money(request.fixedAmount()),
                request.volumeTiers().stream()
                        .map(tier -> new VolumeTier(tier.minQuantity(), tier.discountType(), tier.value()))
                        .toList(),
                request.buyQuantity() == null ? 0 : request.buyQuantity(),
                request.getQuantity() == null ? 0 : request.getQuantity(),
                request.validFrom(),
                request.validTo(),
                money(request.minPurchaseAmount()),
                money(request.maxDiscount()),
                request.maxUsage(),
                request.maxUsagePerCustomer(),
                request.productIds().stream().map(ProductId::of).collect(Collectors.toSet()),
                request.categoryIds().stream().map(CategoryId::of).collect(Collectors.toSet()));
    }

    public UpdatePromotionCommand toUpdateCommand(UpdatePromotionRequest request) {
        return new UpdatePromotionCommand(
                request.name(),
                request.validFrom(),
                request.validTo(),
                money(request.minPurchaseAmount()),
                money(request.maxDiscount()),
                request.maxUsage(),
                request.maxUsagePerCustomer(),
                request.productIds().stream().map(ProductId::of).collect(Collectors.toSet()),
                request.categoryIds().stream().map(CategoryId::of).collect(Collectors.toSet()));
    }

    public PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId().value(),
                promotion.getName(),
                promotion.getType().name(),
                promotion.isActive(),
                promotion.getValidFrom(),
                promotion.getValidTo(),
                promotion.getPercentageValue(),
                toMoneyResponse(promotion.getFixedAmount()),
                promotion.getVolumeTiers().stream()
                        .map(tier -> new PromotionResponse.VolumeTierResponse(
                                tier.minQuantity(), tier.discountType().name(), tier.value()))
                        .toList(),
                promotion.getBuyQuantity(),
                promotion.getGetQuantity(),
                toMoneyResponse(promotion.getMinPurchaseAmount()),
                toMoneyResponse(promotion.getMaxDiscount()),
                promotion.getMaxUsage(),
                promotion.getMaxUsagePerCustomer(),
                promotion.getUsedCount(),
                promotion.getProductIds().stream().map(ProductId::value).collect(Collectors.toSet()),
                promotion.getCategoryIds().stream().map(CategoryId::value).collect(Collectors.toSet()));
    }

    public static Money money(PromotionRequest.MoneyRequest money) {
        return money == null ? null : Money.of(money.amount(), money.currency());
    }

    public static Money money(ApplyCouponRequest.MoneyRequest money) {
        return money == null ? null : Money.of(money.amount(), money.currency());
    }

    public static PromotionResponse.MoneyResponse toMoneyResponse(Money money) {
        return money == null ? null : new PromotionResponse.MoneyResponse(money.amount(), money.currencyCode());
    }
}