package org.fernandodev.ecommerce.search.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResult;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;

/**
 * Casos de uso de búsqueda (puerto de entrada).
 */
public interface SearchUseCases {

    SearchResults<SearchResult> search(SearchQuery query);

    List<String> autocomplete(String q, int limit);

    List<String> suggest(String q, int limit);
}