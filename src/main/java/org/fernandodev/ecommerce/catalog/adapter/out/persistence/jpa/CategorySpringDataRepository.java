package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link CategoryJpaEntity}.
 */
public interface CategorySpringDataRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    boolean existsByParentIdAndDeletedAtIsNull(UUID parentId);

    List<CategoryJpaEntity> findAllByDeletedAtIsNull();
}
