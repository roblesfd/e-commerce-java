package org.fernandodev.ecommerce.catalog.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;

/**
 * Casos de uso del contexto Product Catalog (puerto de entrada) para categorías.
 */
public interface CategoryUseCases {

    Category create(CategoryCommand command);

    Category getById(CategoryId id);

    List<Category> list();

    Category update(CategoryId id, CategoryCommand command);

    Category activate(CategoryId id);

    Category deactivate(CategoryId id);

    void delete(CategoryId id);
}
