package org.fernandodev.ecommerce.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.junit.jupiter.api.Test;

class NotificationPreferencesTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createActivaTodosLosCanales() {
        NotificationPreferences preferences = NotificationPreferences.create(UUID.randomUUID(), T1);

        assertThat(preferences.isEnabled(NotificationChannel.EMAIL)).isTrue();
        assertThat(preferences.isEnabled(NotificationChannel.SMS)).isTrue();
        assertThat(preferences.isEnabled(NotificationChannel.PUSH)).isTrue();
        assertThat(preferences.isEnabled(NotificationChannel.IN_APP)).isTrue();
    }

    @Test
    void setEnabledDesactivaYActivaUnCanal() {
        NotificationPreferences preferences = NotificationPreferences.create(UUID.randomUUID(), T1);

        preferences.setEnabled(NotificationChannel.EMAIL, false, T1);
        assertThat(preferences.isEnabled(NotificationChannel.EMAIL)).isFalse();
        assertThat(preferences.isEnabled(NotificationChannel.SMS)).isTrue();

        preferences.setEnabled(NotificationChannel.EMAIL, true, T1);
        assertThat(preferences.isEnabled(NotificationChannel.EMAIL)).isTrue();
    }
}