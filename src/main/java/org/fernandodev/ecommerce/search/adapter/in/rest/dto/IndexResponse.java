package org.fernandodev.ecommerce.search.adapter.in.rest.dto;

import java.util.UUID;

import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;

/**
 * Respuesta de una operación sobre el índice.
 */
public record IndexResponse(UUID productId, IndexStatus indexStatus, String lastError, int attempts) {
}