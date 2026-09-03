package org.fernandodev.ecommerce.notifications.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.CreateNotificationCommand;
import org.fernandodev.ecommerce.notifications.application.port.in.NotificationUseCases;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationRepository;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationStatus;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de notificaciones.
 * <p>
 * La entrega se realiza al crear: IN_APP se persiste como enviada y los canales
 * externos (EMAIL/SMS/PUSH) se envían a través del proveedor, marcando SENT o
 * FAILED. Las fallidas pueden reintentarse.
 * </p>
 */
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCases {

    private final NotificationRepository notifications;
    private final NotificationProviderPort provider;

    @Override
    @Transactional
    public Notification create(UUID customerId, CreateNotificationCommand command) {
        if (command.dedupKey() != null && notifications.existsByDedupKey(command.dedupKey())) {
            throw new ConflictException("Ya existe una notificación con la clave: " + command.dedupKey());
        }
        Notification notification = Notification.create(NotificationId.generate(), customerId,
                command.channel(), command.title(), command.body(), command.dedupKey(), Instant.now());
        deliver(notification);
        return notifications.save(notification);
    }

    @Override
    public Notification getById(NotificationId id) {
        return getExisting(id);
    }

    @Override
    public List<Notification> listByCustomer(UUID customerId, boolean unreadOnly) {
        return notifications.findByCustomerId(customerId, unreadOnly);
    }

    @Override
    @Transactional
    public Notification markRead(NotificationId id) {
        Notification notification = getExisting(id);
        notification.markRead(Instant.now());
        return notifications.save(notification);
    }

    @Override
    @Transactional
    public Notification markUnread(NotificationId id) {
        Notification notification = getExisting(id);
        notification.markUnread(Instant.now());
        return notifications.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(UUID customerId) {
        notifications.findByCustomerId(customerId, true).forEach(notification -> {
            notification.markRead(Instant.now());
            notifications.save(notification);
        });
    }

    @Override
    @Transactional
    public void delete(NotificationId id) {
        getExisting(id);
        notifications.delete(id);
    }

    @Override
    @Transactional
    public Notification retry(NotificationId id) {
        Notification notification = getExisting(id);
        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new BusinessRuleViolationException("Solo se pueden reintentar notificaciones fallidas");
        }
        notification.retry(Instant.now());
        deliver(notification);
        return notifications.save(notification);
    }

    private void deliver(Notification notification) {
        if (notification.getChannel() == NotificationChannel.IN_APP) {
            notification.markProcessing(Instant.now());
            notification.markSent(Instant.now());
            return;
        }
        notification.markProcessing(Instant.now());
        try {
            NotificationProviderPort.DeliveryResult result = provider.send(
                    notification.getChannel(), notification.getCustomerId(),
                    notification.getTitle(), notification.getBody());
            if (result.success()) {
                notification.markSent(Instant.now());
            } else {
                notification.markFailed(Instant.now());
            }
        } catch (RuntimeException e) {
            notification.markFailed(Instant.now());
        }
    }

    private Notification getExisting(NotificationId id) {
        return notifications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada: " + id));
    }
}