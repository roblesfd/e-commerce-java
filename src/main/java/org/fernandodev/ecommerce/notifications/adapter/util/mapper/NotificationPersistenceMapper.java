package org.fernandodev.ecommerce.notifications.adapter.util.mapper;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationJpaEntity;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;

/**
 * Mapeo entre el agregado {@link Notification} y su entidad JPA.
 */
public final class NotificationPersistenceMapper {

    private NotificationPersistenceMapper() {
    }

    /**
     * @param notification agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId().value());
        entity.setCustomerId(notification.getCustomerId());
        entity.setChannel(notification.getChannel());
        entity.setTitle(notification.getTitle());
        entity.setBody(notification.getBody());
        entity.setStatus(notification.getStatus());
        entity.setRead(notification.isRead());
        entity.setDeliveryAttempts(notification.getDeliveryAttempts());
        entity.setDedupKey(notification.getDedupKey());
        entity.setSentAt(notification.getSentAt());
        entity.setFailedAt(notification.getFailedAt());
        entity.setCreatedAt(notification.getCreatedAt());
        return entity;
    }

    /**
     * @param notificationJpaEntity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.crear(
                NotificationId.of(entity.getId()),
                entity.getCustomerId(),
                entity.getChannel(),
                entity.getTitle(),
                entity.getBody(),
                entity.getStatus(),
                entity.isRead(),
                entity.getDeliveryAttempts(),
                entity.getDedupKey(),
                entity.getSentAt(),
                entity.getFailedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }
}