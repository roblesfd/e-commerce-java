package org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa;

import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.entity.PriceJpaEntity;
import org.fernandodev.ecommerce.pricing.adapter.util.mapper.PricePersistenceMapper;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link PriceRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaPriceRepository implements PriceRepository {

    private final PriceSpringDataRepository springData;

    @Override
    public Price save(Price price) {
        PriceJpaEntity saved = springData.save(PricePersistenceMapper.toEntity(price));
        return PricePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Price> findById(PriceId id) {
        return springData.findById(id.value()).map(PricePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Price> findByProductAndVariant(ProductId productId, ProductVariantId variantId) {
        return springData.findByProductIdAndVariantId(
                        productId.value(),
                        variantId == null ? null : variantId.value())
                .map(PricePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId) {
        return springData.existsByProductIdAndVariantId(
                productId.value(),
                variantId == null ? null : variantId.value());
    }
}