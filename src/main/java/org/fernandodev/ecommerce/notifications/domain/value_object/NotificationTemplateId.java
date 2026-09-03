package org.fernandodev.ecommerce.notifications.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una plantilla de notificación.
 */
public record NotificationTemplateId(UUID value) {

    public NotificationTemplateId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static NotificationTemplateId generate() {
        return new NotificationTemplateId(UUID.randomUUID());
    }

    public static NotificationTemplateId of(UUID value) {
        return new NotificationTemplateId(value);
    }
}