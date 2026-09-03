package org.fernandodev.ecommerce.notifications.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una notificación.
 */
public record NotificationId(UUID value) {

    public NotificationId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }
}