package org.fernandodev.ecommerce.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;

/**
 * Puerto de salida de persistencia del agregado {@link Category}.
 */
public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(CategoryId id);

    boolean existsById(CategoryId id);

    boolean existsByParentId(CategoryId parentId);

    List<Category> findAll();
}