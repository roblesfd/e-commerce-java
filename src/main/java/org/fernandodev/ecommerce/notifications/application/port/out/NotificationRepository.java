package org.fernandodev.ecommerce.notifications.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;

/**
 * Puerto de salida de persistencia del agregado {@link Notification}.
 */
public interface NotificationRepository {

    /**
     * Persiste la notificación.
     */
    Notification save(Notification notification);

    /**
     * Consulta una notificación por identificador.
     */
    Optional<Notification> findById(NotificationId id);

    /**
     * Lista las notificaciones del cliente, opcionalmente solo las no leídas.
     */
    List<Notification> findByCustomerId(UUID customerId, boolean unreadOnly);

    /**
     * Elimina una notificación.
     */
    void delete(NotificationId id);

    /**
     * Indica si ya existe una notificación con esa clave de deduplicación.
     */
    boolean existsByDedupKey(String dedupKey);
}