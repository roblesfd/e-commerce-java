package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationStatus;

/**
 * Respuesta de una notificación.
 */
public record NotificationResponse(
        UUID id,
        UUID customerId,
        NotificationChannel channel,
        String title,
        String body,
        NotificationStatus status,
        boolean read,
        int deliveryAttempts,
        String dedupKey,
        Instant sentAt,
        Instant failedAt,
        Instant createdAt,
        Instant updatedAt
) {
}