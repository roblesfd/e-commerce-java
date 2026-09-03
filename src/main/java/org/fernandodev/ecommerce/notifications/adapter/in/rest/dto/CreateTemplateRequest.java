package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear una plantilla de notificación.
 */
public record CreateTemplateRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String body
) {
}