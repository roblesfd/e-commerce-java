package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.BrandJpaEntity;
import org.fernandodev.ecommerce.catalog.adapter.util.mapper.BrandPersistenceMapper;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link BrandRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaBrandRepository implements BrandRepository {

    private final BrandSpringDataRepository springData;

    @Override
    public Brand save(Brand brand) {
        BrandJpaEntity saved = springData.save(BrandPersistenceMapper.toEntity(brand));
        return BrandPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Brand> findById(BrandId id) {
        return springData.findById(id.value()).map(BrandPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(BrandId id) {
        return springData.existsByIdAndDeletedAtIsNull(id.value());
    }

    @Override
    public List<Brand> findAll() {
        return springData.findAllByDeletedAtIsNull().stream().map(BrandPersistenceMapper::toDomain).toList();
    }
}
