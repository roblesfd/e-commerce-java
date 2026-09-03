package org.fernandodev.ecommerce.search.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.search.adapter.in.rest.dto.IndexResponse;
import org.fernandodev.ecommerce.search.adapter.in.rest.dto.SearchResultsResponse;
import org.fernandodev.ecommerce.search.application.dto.SearchResult;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.springframework.stereotype.Component;

/**
 * Mapeo de los resultados del índice a DTOs REST.
 */
@Component
public class SearchMapper {

    public SearchResultsResponse toResponse(SearchResults<SearchResult> results) {
        return new SearchResultsResponse(
                results.items().stream()
                        .map(result -> new SearchResultsResponse.SearchResultResponse(
                                result.productId(), result.name(), result.sku(), result.barcode(),
                                result.price(), result.currency(), result.available()))
                        .toList(),
                results.total(), results.page(), results.size());
    }

    public IndexResponse toIndexResponse(SearchDocument document) {
        return new IndexResponse(document.getProductId().value(), document.getIndexStatus(),
                document.getLastError(), document.getAttempts());
    }
}