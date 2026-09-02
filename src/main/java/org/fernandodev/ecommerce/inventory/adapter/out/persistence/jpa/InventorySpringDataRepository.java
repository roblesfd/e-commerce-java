package org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.entity.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/**
 * Repositorio Spring Data JPA del agregado {@link InventoryJpaEntity}.
 */
public interface InventorySpringDataRepository extends JpaRepository<InventoryJpaEntity, UUID> {

    Optional<InventoryJpaEntity> findByProductIdAndVariantId(UUID productId, UUID variantId);

    /**
     * Bloquea la fila de inventario (PESSIMISTIC_WRITE) para operaciones
     * concurrentes sensibles. Maneja variantId nulo (inventario a nivel producto).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryJpaEntity i "
            + "where i.productId = :productId "
            + "and (:variantId is null and i.variantId is null or i.variantId = :variantId)")
    Optional<InventoryJpaEntity> findByProductAndVariantForUpdate(@Param("productId") UUID productId,
                                                                  @Param("variantId") UUID variantId);

    boolean existsByProductIdAndVariantId(UUID productId, UUID variantId);
}