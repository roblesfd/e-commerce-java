package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link CategoryJpaEntity}.
 */
public interface CategorySpringDataRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    /**
     * @param id identificador de la categoría.
     * @return {@code true} si existe la categoría (no eliminada).
     */
    boolean existsByIdAndDeletedAtIsNull(UUID id);

    /**
     * @param parentId identificador de la categoría padre.
     * @return {@code true} si existe alguna categoría hija no eliminada del padre.
     */
    boolean existsByParentIdAndDeletedAtIsNull(UUID parentId);

    /**
     * @return todas las categorías no eliminadas.
     */
    List<CategoryJpaEntity> findAllByDeletedAtIsNull();
}
