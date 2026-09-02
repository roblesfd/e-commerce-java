package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.BrandJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link BrandJpaEntity}.
 */
public interface BrandSpringDataRepository extends JpaRepository<BrandJpaEntity, UUID> {

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    List<BrandJpaEntity> findAllByDeletedAtIsNull();
}
