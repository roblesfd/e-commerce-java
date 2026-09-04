package org.fernandodev.ecommerce.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;

/**
 * Puerto de salida de persistencia del agregado {@link Brand}.
 */
public interface BrandRepository {

    /**
     * Persiste la marca.
     *
     * @param brand marca a guardar.
     * @return la marca persistida.
     */
    Brand save(Brand brand);

    /**
     * @param id identificador de la marca.
     * @return la marca, o {@code Optional.empty()} si no existe o está eliminada.
     */
    Optional<Brand> findById(BrandId id);

    /**
     * @param id identificador de la marca.
     * @return {@code true} si existe la marca (no eliminada).
     */
    boolean existsById(BrandId id);

    /**
     * @return todas las marcas no eliminadas.
     */
    List<Brand> findAll();
}