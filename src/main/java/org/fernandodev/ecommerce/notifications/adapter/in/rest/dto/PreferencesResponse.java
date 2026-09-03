package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta de las preferencias de canales de un cliente.
 */
public record PreferencesResponse(
        UUID customerId,
        boolean emailEnabled,
        boolean smsEnabled,
        boolean pushEnabled,
        boolean inAppEnabled,
        Instant updatedAt
) {
}