package org.fernandodev.ecommerce.catalog.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;

/**
 * Casos de uso del contexto Product Catalog (puerto de entrada) para marcas.
 */
public interface BrandUseCases {

    /**
     * Crea una marca activa.
     *
     * @param command datos de la marca.
     * @return la marca creada.
     */
    Brand create(BrandCommand command);

    /**
     * @param id identificador de la marca.
     * @return la marca.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no existe o está eliminada.
     */
    Brand getById(BrandId id);

    /**
     * @return todas las marcas no eliminadas.
     */
    List<Brand> list();

    /**
     * Actualiza el nombre de la marca.
     *
     * @param id      identificador de la marca.
     * @param command nuevos datos de la marca.
     * @return la marca actualizada.
     */
    Brand update(BrandId id, BrandCommand command);

    /**
     * Activa la marca.
     *
     * @param id identificador de la marca.
     * @return la marca activada.
     */
    Brand activate(BrandId id);

    /**
     * Desactiva la marca.
     *
     * @param id identificador de la marca.
     * @return la marca desactivada.
     */
    Brand deactivate(BrandId id);

    /**
     * Elimina la marca (soft delete).
     *
     * @param id identificador de la marca.
     */
    void delete(BrandId id);
}
