package org.fernandodev.ecommerce.notifications.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.CreateNotificationCommand;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;

/**
 * Casos de uso de notificaciones (puerto de entrada).
 */
public interface NotificationUseCases {

    /**
     * Crea y entrega una notificación (con deduplicación por {@code dedupKey}).
     */
    Notification create(UUID customerId, CreateNotificationCommand command);

    /**
     * Consulta una notificación por identificador.
     */
    Notification getById(NotificationId id);

    /**
     * Lista las notificaciones del cliente, opcionalmente solo las no leídas.
     */
    List<Notification> listByCustomer(UUID customerId, boolean unreadOnly);

    /**
     * Marca una notificación como leída.
     */
    Notification markRead(NotificationId id);

    /**
     * Marca una notificación como no leída.
     */
    Notification markUnread(NotificationId id);

    /**
     * Marca todas las notificaciones del cliente como leídas.
     */
    void markAllRead(UUID customerId);

    /**
     * Elimina una notificación.
     */
    void delete(NotificationId id);

    /**
     * Reintenta la entrega de una notificación fallida.
     */
    Notification retry(NotificationId id);
}