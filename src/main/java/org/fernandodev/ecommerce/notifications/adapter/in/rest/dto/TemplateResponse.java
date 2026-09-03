package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta de una plantilla de notificación.
 */
public record TemplateResponse(
        UUID id,
        String code,
        String title,
        String body,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}