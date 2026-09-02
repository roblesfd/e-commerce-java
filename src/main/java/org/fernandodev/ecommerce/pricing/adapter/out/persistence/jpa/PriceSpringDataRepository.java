package org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.entity.PriceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link PriceJpaEntity}.
 */
public interface PriceSpringDataRepository extends JpaRepository<PriceJpaEntity, UUID> {

    Optional<PriceJpaEntity> findByProductIdAndVariantId(UUID productId, UUID variantId);

    boolean existsByProductIdAndVariantId(UUID productId, UUID variantId);
}