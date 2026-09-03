package org.fernandodev.ecommerce.search.application.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Criterios de búsqueda con filtros, orden y paginación simultáneos.
 */
public record SearchQuery(
        String q,
        UUID categoryId,
        UUID brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean available,
        Map<String, String> attributes,
        SortBy sort,
        int page,
        int size
) {

    public SearchQuery {
        attributes = attributes == null ? Map.of() : attributes;
        sort = sort == null ? SortBy.RELEVANCE : sort;
        page = Math.max(page, 0);
        size = size <= 0 ? 20 : Math.min(size, 100);
    }

    public enum SortBy {
        RELEVANCE,
        PRICE_ASC,
        PRICE_DESC,
        NAME_ASC
    }
}