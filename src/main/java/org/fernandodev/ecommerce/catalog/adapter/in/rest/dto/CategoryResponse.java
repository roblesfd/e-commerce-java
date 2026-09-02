package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.UUID;

/**
 * Respuesta con el detalle de una categoría.
 */
public record CategoryResponse(
        UUID id,
        String name,
        UUID parentId,
        boolean active
) {
}