package org.fernandodev.ecommerce.search.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Documento del índice devuelto por una búsqueda.
 */
public record SearchResult(
        UUID productId,
        String name,
        String sku,
        String barcode,
        BigDecimal price,
        String currency,
        boolean available
) {
}