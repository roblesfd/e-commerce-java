package org.fernandodev.ecommerce.notifications.adapter.in.rest.dto;

/**
 * Petición para actualizar las preferencias de canales.
 */
public record UpdatePreferencesRequest(
        boolean emailEnabled,
        boolean smsEnabled,
        boolean pushEnabled,
        boolean inAppEnabled
) {
}