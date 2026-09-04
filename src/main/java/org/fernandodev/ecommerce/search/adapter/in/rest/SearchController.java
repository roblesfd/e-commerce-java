package org.fernandodev.ecommerce.search.adapter.in.rest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fernandodev.ecommerce.search.adapter.in.rest.dto.SearchResultsResponse;
import org.fernandodev.ecommerce.search.adapter.in.rest.mapper.SearchMapper;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.port.in.SearchUseCases;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de búsqueda y sugerencias.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private static final int DEFAULT_LIMIT = 8;

    private final SearchUseCases searchUseCases;
    private final SearchMapper mapper;

    @GetMapping("/products")
    public SearchResultsResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) List<String> attribute,
            @RequestParam(required = false) SearchQuery.SortBy sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchQuery query = new SearchQuery(q, categoryId, brandId, minPrice, maxPrice, available,
                parseAttributes(attribute), sort, page, size);
        return mapper.toResponse(searchUseCases.search(query));
    }

    @GetMapping("/autocomplete")
    /**
     * Autocomplete.
     * @param q q.
     * @param false false.
     * @param limit limit.
     */
    public List<String> autocomplete(@RequestParam String q,
                                     @RequestParam(required = false, defaultValue = "8") int limit) {
        return searchUseCases.autocomplete(q, limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 20));
    }

    @GetMapping("/suggest")
    /**
     * Suggest.
     * @param q q.
     * @param false false.
     * @param limit limit.
     */
    public List<String> suggest(@RequestParam String q,
                                @RequestParam(required = false, defaultValue = "8") int limit) {
        return searchUseCases.suggest(q, limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 20));
    }

    private Map<String, String> parseAttributes(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return Map.of();
        }
        Map<String, String> attributes = new HashMap<>();
        attribute.forEach(raw -> {
            int separator = raw.indexOf(':');
            if (separator > 0) {
                attributes.put(raw.substring(0, separator), raw.substring(separator + 1));
            }
        });
        return attributes;
    }
}