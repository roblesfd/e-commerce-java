package org.fernandodev.ecommerce.notifications.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una plantilla de notificación.
 */
public record NotificationTemplateId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public NotificationTemplateId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static NotificationTemplateId generate() {
        return new NotificationTemplateId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link NotificationTemplateId} con el valor dado.
     */
    public static NotificationTemplateId of(UUID value) {
        return new NotificationTemplateId(value);
    }
}