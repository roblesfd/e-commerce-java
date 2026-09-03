package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para actualizar una plantilla de notificación.
 */
public record UpdateTemplateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String body
) {
}