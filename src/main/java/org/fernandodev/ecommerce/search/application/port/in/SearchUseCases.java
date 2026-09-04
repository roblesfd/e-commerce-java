package org.fernandodev.ecommerce.search.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.dto.SearchResult;
import org.fernandodev.ecommerce.search.application.dto.SearchResults;

/**
 * Casos de uso de búsqueda (puerto de entrada).
 */
public interface SearchUseCases {

    /**
     * Busca productos con filtros, orden y paginación.
     */
    SearchResults<SearchResult> search(SearchQuery query);

    /**
     * Devuelve sugerencias por prefijo del término.
     */
    List<String> autocomplete(String q, int limit);

    /**
     * Devuelve sugerencias por coincidencia del término.
     */
    List<String> suggest(String q, int limit);
}