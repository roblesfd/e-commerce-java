package org.fernandodev.ecommerce.catalog.application.port.in;

import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;

/**
 * Casos de uso del contexto Product Catalog (puerto de entrada) para productos.
 */
public interface ProductUseCases {

    Product create(ProductCommand command);

    Product getById(ProductId id);

    PageResult<Product> list(ProductSearch search, PageQuery query);

    Product update(ProductId id, ProductCommand command);

    Product activate(ProductId id);

    Product deactivate(ProductId id);

    void delete(ProductId id);
}
