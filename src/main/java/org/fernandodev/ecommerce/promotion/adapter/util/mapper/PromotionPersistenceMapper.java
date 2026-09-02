package org.fernandodev.ecommerce.promotion.adapter.util.mapper;

import java.util.LinkedHashSet;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.CustomerUsageEmbeddable;
import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.VolumeTierEmbeddable;
import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity.PromotionJpaEntity;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CustomerUsage;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.promotion.domain.value_object.VolumeTier;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Promotion} y la entidad JPA
 * {@link PromotionJpaEntity}. Sin lógica de negocio.
 */
public final class PromotionPersistenceMapper {

    private PromotionPersistenceMapper() {
    }

    public static PromotionJpaEntity toEntity(Promotion promotion) {
        PromotionJpaEntity entity = new PromotionJpaEntity();
        entity.setId(promotion.getId().value());
        entity.setName(promotion.getName());
        entity.setType(promotion.getType());
        entity.setActive(promotion.isActive());
        entity.setValidFrom(promotion.getValidFrom());
        entity.setValidTo(promotion.getValidTo());
        entity.setPercentageValue(promotion.getPercentageValue());
        if (promotion.getFixedAmount() != null) {
            entity.setFixedAmount(promotion.getFixedAmount().amount());
            entity.setFixedCurrency(promotion.getFixedAmount().currencyCode());
        }
        entity.setBuyQuantity(promotion.getBuyQuantity());
        entity.setGetQuantity(promotion.getGetQuantity());
        if (promotion.getMinPurchaseAmount() != null) {
            entity.setMinPurchaseAmount(promotion.getMinPurchaseAmount().amount());
            entity.setMinPurchaseCurrency(promotion.getMinPurchaseAmount().currencyCode());
        }
        if (promotion.getMaxDiscount() != null) {
            entity.setMaxDiscountAmount(promotion.getMaxDiscount().amount());
            entity.setMaxDiscountCurrency(promotion.getMaxDiscount().currencyCode());
        }
        entity.setMaxUsage(promotion.getMaxUsage());
        entity.setMaxUsagePerCustomer(promotion.getMaxUsagePerCustomer());
        entity.setUsedCount(promotion.getUsedCount());
        promotion.getProductIds().forEach(id -> entity.getProductIds().add(id.value()));
        promotion.getCategoryIds().forEach(id -> entity.getCategoryIds().add(id.value()));
        promotion.getVolumeTiers().forEach(tier -> entity.getVolumeTiers().add(toTierEmbeddable(tier)));
        promotion.getUsageByCustomer().forEach(usage -> entity.getUsageByCustomer().add(toUsageEmbeddable(usage)));
        return entity;
    }

    public static Promotion toDomain(PromotionJpaEntity entity) {
        return Promotion.crear(
                PromotionId.of(entity.getId()),
                entity.getName(),
                entity.getType(),
                entity.isActive(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getPercentageValue(),
                entity.getFixedAmount() == null ? null : Money.of(entity.getFixedAmount(), entity.getFixedCurrency()),
                entity.getVolumeTiers().stream().map(PromotionPersistenceMapper::toDomainTier).toList(),
                entity.getBuyQuantity() == null ? 0 : entity.getBuyQuantity(),
                entity.getGetQuantity() == null ? 0 : entity.getGetQuantity(),
                entity.getMinPurchaseAmount() == null ? null
                        : Money.of(entity.getMinPurchaseAmount(), entity.getMinPurchaseCurrency()),
                entity.getMaxDiscountAmount() == null ? null
                        : Money.of(entity.getMaxDiscountAmount(), entity.getMaxDiscountCurrency()),
                entity.getMaxUsage(),
                entity.getMaxUsagePerCustomer(),
                entity.getUsedCount(),
                toProductIds(entity.getProductIds()),
                toCategoryIds(entity.getCategoryIds()),
                entity.getUsageByCustomer().stream().map(PromotionPersistenceMapper::toDomainUsage).toList());
    }

    private static VolumeTierEmbeddable toTierEmbeddable(VolumeTier tier) {
        VolumeTierEmbeddable embeddable = new VolumeTierEmbeddable();
        embeddable.setMinQuantity(tier.minQuantity());
        embeddable.setDiscountType(tier.discountType());
        embeddable.setValue(tier.value());
        return embeddable;
    }

    private static VolumeTier toDomainTier(VolumeTierEmbeddable embeddable) {
        return new VolumeTier(embeddable.getMinQuantity(), embeddable.getDiscountType(), embeddable.getValue());
    }

    private static CustomerUsageEmbeddable toUsageEmbeddable(CustomerUsage usage) {
        CustomerUsageEmbeddable embeddable = new CustomerUsageEmbeddable();
        embeddable.setCustomerId(usage.customerId());
        embeddable.setCount(usage.count());
        return embeddable;
    }

    private static CustomerUsage toDomainUsage(CustomerUsageEmbeddable embeddable) {
        return new CustomerUsage(embeddable.getCustomerId(), embeddable.getCount());
    }

    private static Set<ProductId> toProductIds(Set<java.util.UUID> ids) {
        Set<ProductId> result = new LinkedHashSet<>();
        ids.forEach(id -> result.add(ProductId.of(id)));
        return result;
    }

    private static Set<CategoryId> toCategoryIds(Set<java.util.UUID> ids) {
        Set<CategoryId> result = new LinkedHashSet<>();
        ids.forEach(id -> result.add(CategoryId.of(id)));
        return result;
    }
}