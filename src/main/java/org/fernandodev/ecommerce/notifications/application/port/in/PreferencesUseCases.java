package org.fernandodev.ecommerce.notifications.application.port.in;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.UpdatePreferencesCommand;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;

/**
 * Casos de uso de preferencias de canales.
 */
public interface PreferencesUseCases {

    /**
     * Obtiene las preferencias del cliente (las crea por defecto si no existen).
     */
    NotificationPreferences getByCustomer(UUID customerId);

    /**
     * Actualiza los canales habilitados del cliente.
     */
    NotificationPreferences update(UUID customerId, UpdatePreferencesCommand command);
}