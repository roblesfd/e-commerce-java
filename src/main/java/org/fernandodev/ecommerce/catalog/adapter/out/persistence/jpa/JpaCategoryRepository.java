package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.fernandodev.ecommerce.catalog.adapter.util.mapper.CategoryPersistenceMapper;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link CategoryRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaCategoryRepository implements CategoryRepository {

    private final CategorySpringDataRepository springData;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity saved = springData.save(CategoryPersistenceMapper.toEntity(category));
        return CategoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return springData.findById(id.value()).map(CategoryPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(CategoryId id) {
        return springData.existsByIdAndDeletedAtIsNull(id.value());
    }

    @Override
    public boolean existsByParentId(CategoryId parentId) {
        return springData.existsByParentIdAndDeletedAtIsNull(parentId.value());
    }

    @Override
    public List<Category> findAll() {
        return springData.findAllByDeletedAtIsNull().stream().map(CategoryPersistenceMapper::toDomain).toList();
    }
}
