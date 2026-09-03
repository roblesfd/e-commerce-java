package org.fernandodev.ecommerce.notifications.adapter.in.rest.mapper;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.CreateNotificationRequest;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.NotificationResponse;
import org.fernandodev.ecommerce.notifications.application.dto.CreateNotificationCommand;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link Notification} y sus DTOs REST.
 */
@Component
public class NotificationMapper {

    public CreateNotificationCommand toCommand(CreateNotificationRequest request) {
        return new CreateNotificationCommand(request.channel(), request.title(), request.body(), request.dedupKey());
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId().value(),
                notification.getCustomerId(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getBody(),
                notification.getStatus(),
                notification.isRead(),
                notification.getDeliveryAttempts(),
                notification.getDedupKey(),
                notification.getSentAt(),
                notification.getFailedAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt());
    }
}