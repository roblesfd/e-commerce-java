package org.fernandodev.ecommerce.search.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;

/**
 * Puerto de salida de persistencia del índice de búsqueda.
 */
public interface SearchDocumentRepository {

    /**
     * Persiste el documento del índice.
     */
    SearchDocument save(SearchDocument document);

    /**
     * Consulta un documento por producto.
     */
    Optional<SearchDocument> findById(ProductId productId);

    /**
     * Lista documentos por estado de indexación.
     */
    List<SearchDocument> findByIndexStatus(IndexStatus status);

    /**
     * Elimina el documento del producto.
     */
    void delete(ProductId productId);

    /**
     * Lista todos los productos indexados.
     */
    List<ProductId> findAllProductIds();

    /**
     * Busca documentos con filtros, orden y paginación.
     */
    SearchResults<SearchDocument> search(SearchQuery query);

    /**
     * Devuelve nombres por prefijo.
     */
    List<String> findAutocompleteNames(String prefix, int limit);

    /**
     * Devuelve nombres por coincidencia.
     */
    List<String> findSuggestions(String q, int limit);
}