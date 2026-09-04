package org.fernandodev.ecommerce.catalog.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;

/**
 * Casos de uso del contexto Product Catalog (puerto de entrada) para categorías.
 */
public interface CategoryUseCases {

    /**
     * Crea una categoría activa.
     *
     * @param command datos de la categoría.
     * @return la categoría creada.
     */
    Category create(CategoryCommand command);

    /**
     * @param id identificador de la categoría.
     * @return la categoría.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no existe o está eliminada.
     */
    Category getById(CategoryId id);

    /**
     * @return todas las categorías no eliminadas.
     */
    List<Category> list();

    /**
     * Actualiza nombre y padre de la categoría.
     *
     * @param id      identificador de la categoría.
     * @param command nuevos datos de la categoría.
     * @return la categoría actualizada.
     */
    Category update(CategoryId id, CategoryCommand command);

    /**
     * Activa la categoría.
     *
     * @param id identificador de la categoría.
     * @return la categoría activada.
     */
    Category activate(CategoryId id);

    /**
     * Desactiva la categoría.
     *
     * @param id identificador de la categoría.
     * @return la categoría desactivada.
     */
    Category deactivate(CategoryId id);

    /**
     * Elimina la categoría (soft delete).
     *
     * @param id identificador de la categoría.
     */
    void delete(CategoryId id);
}
