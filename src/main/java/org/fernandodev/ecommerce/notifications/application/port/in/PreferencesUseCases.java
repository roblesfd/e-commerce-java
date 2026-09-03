package org.fernandodev.ecommerce.notifications.application.port.in;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.UpdatePreferencesCommand;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;

/**
 * Casos de uso de preferencias de canales.
 */
public interface PreferencesUseCases {

    NotificationPreferences getByCustomer(UUID customerId);

    NotificationPreferences update(UUID customerId, UpdatePreferencesCommand command);
}