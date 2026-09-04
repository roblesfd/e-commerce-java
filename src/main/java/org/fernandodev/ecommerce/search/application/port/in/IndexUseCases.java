package org.fernandodev.ecommerce.search.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.domain.SearchDocument;

/**
 * Casos de uso de mantenimiento del índice de búsqueda.
 */
public interface IndexUseCases {

    /**
     * Indexa un producto leyendo Catalog, Pricing e Inventory.
     */
    SearchDocument indexProduct(ProductId productId);

    /**
     * Elimina un producto del índice.
     */
    void removeProduct(ProductId productId);

    /**
     * Reindexa todos los productos del catálogo.
     */
    void rebuild();

    /**
     * Reintenta la indexación de un documento fallido.
     */
    SearchDocument retryIndexing(ProductId productId);

    /**
     * Lista los documentos con indexación fallida.
     */
    List<SearchDocument> listIndexFailures();
}