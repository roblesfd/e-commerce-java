package org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.entity.PriceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link PriceJpaEntity}.
 */
public interface PriceSpringDataRepository extends JpaRepository<PriceJpaEntity, UUID> {

    /**
     * @param productId producto.
     * @param variantId variante (null si es a nivel producto).
     * @return el precio del producto/variante, o {@code Optional.empty()} si no existe.
     */
    Optional<PriceJpaEntity> findByProductIdAndVariantId(UUID productId, UUID variantId);

    /**
     * @param productId producto.
     * @param variantId variante (null si es a nivel producto).
     * @return {@code true} si existe un precio para el producto/variante.
     */
    boolean existsByProductIdAndVariantId(UUID productId, UUID variantId);
}