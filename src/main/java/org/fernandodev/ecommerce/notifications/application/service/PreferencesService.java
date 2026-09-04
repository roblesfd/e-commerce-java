package org.fernandodev.ecommerce.notifications.application.service;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.UpdatePreferencesCommand;
import org.fernandodev.ecommerce.notifications.application.port.in.PreferencesUseCases;
import org.fernandodev.ecommerce.notifications.application.port.out.PreferencesRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de preferencias de canales.
 */
@RequiredArgsConstructor
public class PreferencesService implements PreferencesUseCases {

    private final PreferencesRepository preferences;

    /**

     * {@inheritDoc}

     */

    @Override
    public NotificationPreferences getByCustomer(UUID customerId) {
        return preferences.findByCustomerId(customerId)
                .orElseGet(() -> preferences.save(NotificationPreferences.create(customerId, Instant.now())));
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public NotificationPreferences update(UUID customerId, UpdatePreferencesCommand command) {
        NotificationPreferences prefs = getByCustomer(customerId);
        prefs.setEnabled(NotificationChannel.EMAIL, command.emailEnabled(), Instant.now());
        prefs.setEnabled(NotificationChannel.SMS, command.smsEnabled(), Instant.now());
        prefs.setEnabled(NotificationChannel.PUSH, command.pushEnabled(), Instant.now());
        prefs.setEnabled(NotificationChannel.IN_APP, command.inAppEnabled(), Instant.now());
        return preferences.save(prefs);
    }
}