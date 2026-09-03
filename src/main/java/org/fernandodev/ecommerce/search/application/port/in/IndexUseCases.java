package org.fernandodev.ecommerce.search.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.domain.SearchDocument;

/**
 * Casos de uso de mantenimiento del índice de búsqueda.
 */
public interface IndexUseCases {

    SearchDocument indexProduct(ProductId productId);

    void removeProduct(ProductId productId);

    void rebuild();

    SearchDocument retryIndexing(ProductId productId);

    List<SearchDocument> listIndexFailures();
}