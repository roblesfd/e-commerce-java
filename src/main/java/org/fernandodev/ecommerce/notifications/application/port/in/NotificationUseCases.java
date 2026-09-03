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

    Notification create(UUID customerId, CreateNotificationCommand command);

    Notification getById(NotificationId id);

    List<Notification> listByCustomer(UUID customerId, boolean unreadOnly);

    Notification markRead(NotificationId id);

    Notification markUnread(NotificationId id);

    void markAllRead(UUID customerId);

    void delete(NotificationId id);

    Notification retry(NotificationId id);
}