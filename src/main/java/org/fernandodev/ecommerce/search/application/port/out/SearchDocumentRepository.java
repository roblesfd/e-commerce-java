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

    SearchDocument save(SearchDocument document);

    Optional<SearchDocument> findById(ProductId productId);

    List<SearchDocument> findByIndexStatus(IndexStatus status);

    void delete(ProductId productId);

    List<ProductId> findAllProductIds();

    SearchResults<SearchDocument> search(SearchQuery query);

    List<String> findAutocompleteNames(String prefix, int limit);

    List<String> findSuggestions(String q, int limit);
}