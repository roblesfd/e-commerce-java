package org.fernandodev.ecommerce.search.application.service;

import java.util.List;

import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResult;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;
import org.fernandodev.ecommerce.search.application.port.in.SearchUseCases;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de búsqueda sobre el índice denormalizado.
 */
@RequiredArgsConstructor
public class SearchService implements SearchUseCases {

    private final SearchDocumentRepository index;

    @Override
    public SearchResults<SearchResult> search(SearchQuery query) {
        SearchResults<SearchDocument> results = index.search(query);
        List<SearchResult> items = results.items().stream()
                .map(this::toResult)
                .toList();
        return new SearchResults<>(items, results.total(), results.page(), results.size());
    }

    @Override
    public List<String> autocomplete(String q, int limit) {
        return index.findAutocompleteNames(normalize(q), limit);
    }

    @Override
    public List<String> suggest(String q, int limit) {
        return index.findSuggestions(normalize(q), limit);
    }

    private SearchResult toResult(SearchDocument document) {
        return new SearchResult(
                document.getProductId().value(),
                document.getName(),
                document.getSku(),
                document.getBarcode(),
                document.getPrice() == null ? null : document.getPrice().amount(),
                document.getPrice() == null ? null : document.getPrice().currencyCode(),
                document.isAvailable());
    }

    private String normalize(String q) {
        return q == null ? "" : q.trim().toLowerCase();
    }
}