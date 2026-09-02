package org.fernandodev.ecommerce.catalog.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;

/**
 * Casos de uso del contexto Product Catalog (puerto de entrada) para marcas.
 */
public interface BrandUseCases {

    Brand create(BrandCommand command);

    Brand getById(BrandId id);

    List<Brand> list();

    Brand update(BrandId id, BrandCommand command);

    Brand activate(BrandId id);

    Brand deactivate(BrandId id);

    void delete(BrandId id);
}
