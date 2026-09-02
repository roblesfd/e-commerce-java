package org.fernandodev.ecommerce.pricing.adapter.util.mapper;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.PriceChangeEmbeddable;
import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.entity.PriceJpaEntity;
import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.entity.PriceOverrideJpaEntity;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceChange;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverride;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Price} y la entidad JPA
 * {@link PriceJpaEntity}. Sin lógica de negocio.
 */
public final class PricePersistenceMapper {

    private PricePersistenceMapper() {
    }

    public static PriceJpaEntity toEntity(Price price) {
        PriceJpaEntity entity = new PriceJpaEntity();
        entity.setId(price.getId().value());
        entity.setProductId(price.getProductId().value());
        entity.setVariantId(price.getVariantId().map(ProductVariantId::value).orElse(null));
        entity.setBaseAmount(price.getBasePrice().amount());
        entity.setCurrency(price.getBasePrice().currencyCode());
        price.getHistory().forEach(change -> entity.getHistory().add(toChangeEmbeddable(change)));
        price.getOverrides().forEach(override -> entity.getOverrides().add(toOverrideEntity(override, entity)));
        return entity;
    }

    public static Price toDomain(PriceJpaEntity entity) {
        return Price.crear(
                PriceId.of(entity.getId()),
                ProductId.of(entity.getProductId()),
                entity.getVariantId() == null ? null : ProductVariantId.of(entity.getVariantId()),
                Money.of(entity.getBaseAmount(), entity.getCurrency()),
                entity.getHistory().stream().map(PricePersistenceMapper::toDomainChange).toList(),
                entity.getOverrides().stream().map(PricePersistenceMapper::toDomainOverride).toList());
    }

    private static PriceChangeEmbeddable toChangeEmbeddable(PriceChange change) {
        PriceChangeEmbeddable embeddable = new PriceChangeEmbeddable();
        embeddable.setEffectiveAt(change.effectiveAt());
        embeddable.setAmount(change.amount().amount());
        embeddable.setCurrency(change.amount().currencyCode());
        return embeddable;
    }

    private static PriceOverrideJpaEntity toOverrideEntity(PriceOverride override, PriceJpaEntity price) {
        PriceOverrideJpaEntity entity = new PriceOverrideJpaEntity();
        entity.setId(override.id().value());
        entity.setPrice(price);
        entity.setAmount(override.amount().amount());
        entity.setCurrency(override.amount().currencyCode());
        entity.setValidFrom(override.validFrom());
        entity.setValidTo(override.validTo());
        entity.setType(override.type());
        return entity;
    }

    private static PriceChange toDomainChange(PriceChangeEmbeddable embeddable) {
        return new PriceChange(Money.of(embeddable.getAmount(), embeddable.getCurrency()), embeddable.getEffectiveAt());
    }

    private static PriceOverride toDomainOverride(PriceOverrideJpaEntity entity) {
        return new PriceOverride(
                PriceOverrideId.of(entity.getId()),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getType());
    }
}