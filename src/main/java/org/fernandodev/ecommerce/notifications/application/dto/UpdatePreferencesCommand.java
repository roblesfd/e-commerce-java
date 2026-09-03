package org.fernandodev.ecommerce.notifications.application.dto;

/**
 * Preferencias completas de canales de un cliente.
 */
public record UpdatePreferencesCommand(
        boolean emailEnabled,
        boolean smsEnabled,
        boolean pushEnabled,
        boolean inAppEnabled
) {
}