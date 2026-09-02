package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.UUID;

/**
 * Respuesta con el detalle de una marca.
 */
public record BrandResponse(
        UUID id,
        String name,
        boolean active
) {
}