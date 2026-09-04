package org.fernandodev.ecommerce.notifications.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una notificación.
 */
public record NotificationId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public NotificationId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link NotificationId} con el valor dado.
     */
    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }
}