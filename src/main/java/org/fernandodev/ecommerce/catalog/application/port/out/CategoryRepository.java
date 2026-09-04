package org.fernandodev.ecommerce.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;

/**
 * Puerto de salida de persistencia del agregado {@link Category}.
 */
public interface CategoryRepository {

    /**
     * Persiste la categoría.
     *
     * @param category categoría a guardar.
     * @return la categoría persistida.
     */
    Category save(Category category);

    /**
     * @param id identificador de la categoría.
     * @return la categoría, o {@code Optional.empty()} si no existe o está eliminada.
     */
    Optional<Category> findById(CategoryId id);

    /**
     * @param id identificador de la categoría.
     * @return {@code true} si existe la categoría (no eliminada).
     */
    boolean existsById(CategoryId id);

    /**
     * @param parentId identificador de la categoría padre.
     * @return {@code true} si existe alguna categoría hija no eliminada del padre.
     */
    boolean existsByParentId(CategoryId parentId);

    /**
     * @return todas las categorías no eliminadas.
     */
    List<Category> findAll();
}