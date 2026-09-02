package org.fernandodev.ecommerce.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;

/**
 * Puerto de salida de persistencia del agregado {@link Brand}.
 */
public interface BrandRepository {

    Brand save(Brand brand);

    Optional<Brand> findById(BrandId id);

    boolean existsById(BrandId id);

    List<Brand> findAll();
}