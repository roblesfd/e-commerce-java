package org.fernandodev.ecommerce.notifications.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;

/**
 * Puerto de salida de persistencia del agregado {@link NotificationPreferences}.
 */
public interface PreferencesRepository {

    /**
     * Persiste las preferencias.
     */
    NotificationPreferences save(NotificationPreferences preferences);

    /**
     * Consulta las preferencias del cliente.
     */
    Optional<NotificationPreferences> findByCustomerId(UUID customerId);
}