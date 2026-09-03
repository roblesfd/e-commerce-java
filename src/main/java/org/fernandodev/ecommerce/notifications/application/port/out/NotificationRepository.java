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

    Notification save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    List<Notification> findByCustomerId(UUID customerId, boolean unreadOnly);

    void delete(NotificationId id);

    boolean existsByDedupKey(String dedupKey);
}