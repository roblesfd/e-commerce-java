package org.fernandodev.ecommerce.search.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta de una búsqueda.
 */
public record SearchResultsResponse(
        List<SearchResultResponse> items,
        long total,
        int page,
        int size
) {

    public record SearchResultResponse(
            UUID productId,
            String name,
            String sku,
            String barcode,
            BigDecimal price,
            String currency,
            boolean available
    ) {
    }
}