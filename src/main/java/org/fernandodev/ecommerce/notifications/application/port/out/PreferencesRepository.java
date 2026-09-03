package org.fernandodev.ecommerce.notifications.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;

/**
 * Puerto de salida de persistencia del agregado {@link NotificationPreferences}.
 */
public interface PreferencesRepository {

    NotificationPreferences save(NotificationPreferences preferences);

    Optional<NotificationPreferences> findByCustomerId(UUID customerId);
}