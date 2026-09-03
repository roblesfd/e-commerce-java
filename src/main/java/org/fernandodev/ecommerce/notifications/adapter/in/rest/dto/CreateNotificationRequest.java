package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear una notificación (operación de sistema).
 */
public record CreateNotificationRequest(
        @NotNull UUID customerId,
        @NotNull NotificationChannel channel,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String body,
        @Size(max = 100) String dedupKey
) {
}