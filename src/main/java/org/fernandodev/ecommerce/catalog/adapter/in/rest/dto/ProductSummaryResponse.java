package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.UUID;

/**
 * Resumen de un producto para listados.
 */
public record ProductSummaryResponse(
        UUID id,
        String sku,
        String name,
        boolean active
) {
}