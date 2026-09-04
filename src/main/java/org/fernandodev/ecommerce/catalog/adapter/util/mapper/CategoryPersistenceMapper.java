package org.fernandodev.ecommerce.catalog.adapter.util.mapper;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;

/**
 * Mapper manual entre el agregado de dominio {@link Category} y la entidad JPA
 * {@link CategoryJpaEntity}. Sin lógica de negocio.
 */
public final class CategoryPersistenceMapper {

    private CategoryPersistenceMapper() {
    }

    /**
     * @param category agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static CategoryJpaEntity toEntity(Category category) {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setId(category.getId().value());
        entity.setParentId(category.getParentId().map(CategoryId::value).orElse(null));
        entity.setName(category.getName());
        entity.setActive(category.isActive());
        entity.setDeletedAt(category.getDeletedAt().orElse(null));
        return entity;
    }

    /**
     * @param entity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static Category toDomain(CategoryJpaEntity entity) {
        return Category.crear(
                CategoryId.of(entity.getId()),
                entity.getName(),
                entity.getParentId() == null ? null : CategoryId.of(entity.getParentId()),
                entity.isActive(),
                entity.getDeletedAt());
    }
}
