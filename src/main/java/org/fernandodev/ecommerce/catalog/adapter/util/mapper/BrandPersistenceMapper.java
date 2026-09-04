package org.fernandodev.ecommerce.catalog.adapter.util.mapper;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.BrandJpaEntity;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;

/**
 * Mapper manual entre el agregado de dominio {@link Brand} y la entidad JPA
 * {@link BrandJpaEntity}. Sin lógica de negocio.
 */
public final class BrandPersistenceMapper {

    private BrandPersistenceMapper() {
    }

    /**
     * @param brand agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static BrandJpaEntity toEntity(Brand brand) {
        BrandJpaEntity entity = new BrandJpaEntity();
        entity.setId(brand.getId().value());
        entity.setName(brand.getName());
        entity.setActive(brand.isActive());
        entity.setDeletedAt(brand.getDeletedAt().orElse(null));
        return entity;
    }

    /**
     * @param entity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static Brand toDomain(BrandJpaEntity entity) {
        return Brand.crear(
                BrandId.of(entity.getId()),
                entity.getName(),
                entity.isActive(),
                entity.getDeletedAt());
    }
}
