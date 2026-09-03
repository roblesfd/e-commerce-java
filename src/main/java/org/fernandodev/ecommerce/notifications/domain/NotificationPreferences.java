package org.fernandodev.ecommerce.notifications.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Preferencias de canales de notificación por cliente.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationPreferences {

    private UUID customerId;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;
    private Instant updatedAt;

    public static NotificationPreferences create(UUID customerId, Instant at) {
        return crear(customerId, true, true, true, true, at);
    }

    public static NotificationPreferences crear(UUID customerId, boolean emailEnabled, boolean smsEnabled,
                                                boolean pushEnabled, boolean inAppEnabled, Instant updatedAt) {
        NotificationPreferences preferences = new NotificationPreferences();
        preferences.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        preferences.emailEnabled = emailEnabled;
        preferences.smsEnabled = smsEnabled;
        preferences.pushEnabled = pushEnabled;
        preferences.inAppEnabled = inAppEnabled;
        preferences.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return preferences;
    }

    public boolean isEnabled(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case SMS -> smsEnabled;
            case PUSH -> pushEnabled;
            case IN_APP -> inAppEnabled;
        };
    }

    public void setEnabled(NotificationChannel channel, boolean enabled, Instant at) {
        switch (channel) {
            case EMAIL -> this.emailEnabled = enabled;
            case SMS -> this.smsEnabled = enabled;
            case PUSH -> this.pushEnabled = enabled;
            case IN_APP -> this.inAppEnabled = enabled;
        }
        this.updatedAt = at;
    }
}