package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.List;

/**
 * Respuesta paginada de una consulta.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}