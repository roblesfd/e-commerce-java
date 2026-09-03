package org.fernandodev.ecommerce.notifications.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationStatus;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una notificación y su estado de entrega.
 * <p>
 * El ciclo de vida es PENDING → PROCESSING → SENT | FAILED, con reintento desde
 * FAILED a PENDING. La lectura (in-app) se trackea de forma independiente.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification {

    private NotificationId id;
    private UUID customerId;
    private NotificationChannel channel;
    private String title;
    private String body;
    private NotificationStatus status;
    private boolean read;
    private int deliveryAttempts;
    private String dedupKey;
    private Instant sentAt;
    private Instant failedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static Notification create(NotificationId id, UUID customerId, NotificationChannel channel,
                                      String title, String body, String dedupKey, Instant at) {
        Notification notification = new Notification();
        notification.id = Objects.requireNonNull(id, "id no puede ser null");
        notification.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        notification.channel = Objects.requireNonNull(channel, "channel no puede ser null");
        notification.title = Objects.requireNonNull(title, "title no puede ser null");
        notification.body = Objects.requireNonNull(body, "body no puede ser null");
        notification.status = NotificationStatus.PENDING;
        notification.read = false;
        notification.deliveryAttempts = 0;
        notification.dedupKey = dedupKey;
        notification.sentAt = null;
        notification.failedAt = null;
        notification.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        notification.updatedAt = at;
        return notification;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Notification crear(
            NotificationId id,
            UUID customerId,
            NotificationChannel channel,
            String title,
            String body,
            NotificationStatus status,
            boolean read,
            int deliveryAttempts,
            String dedupKey,
            Instant sentAt,
            Instant failedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        Notification notification = new Notification();
        notification.id = Objects.requireNonNull(id, "id no puede ser null");
        notification.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        notification.channel = Objects.requireNonNull(channel, "channel no puede ser null");
        notification.title = Objects.requireNonNull(title, "title no puede ser null");
        notification.body = Objects.requireNonNull(body, "body no puede ser null");
        notification.status = Objects.requireNonNull(status, "status no puede ser null");
        notification.read = read;
        notification.deliveryAttempts = deliveryAttempts;
        notification.dedupKey = dedupKey;
        notification.sentAt = sentAt;
        notification.failedAt = failedAt;
        notification.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        notification.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return notification;
    }

    public void markProcessing(Instant at) {
        requireTransition(NotificationStatus.PROCESSING, "procesar");
        this.deliveryAttempts++;
        this.updatedAt = at;
    }

    public void markSent(Instant at) {
        requireTransition(NotificationStatus.SENT, "marcar como enviada");
        this.sentAt = at;
        this.failedAt = null;
        this.updatedAt = at;
    }

    public void markFailed(Instant at) {
        if (status != NotificationStatus.PROCESSING && status != NotificationStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede fallar una notificación PENDING o PROCESSING");
        }
        this.status = NotificationStatus.FAILED;
        this.failedAt = at;
        this.updatedAt = at;
    }

    public void retry(Instant at) {
        if (status != NotificationStatus.FAILED) {
            throw new BusinessRuleViolationException("Solo se puede reintentar una notificación fallida");
        }
        this.status = NotificationStatus.PENDING;
        this.failedAt = null;
        this.updatedAt = at;
    }

    public void markRead(Instant at) {
        if (status != NotificationStatus.SENT) {
            throw new BusinessRuleViolationException("Solo se puede leer una notificación enviada");
        }
        this.read = true;
        this.updatedAt = at;
    }

    public void markUnread(Instant at) {
        if (status != NotificationStatus.SENT) {
            throw new BusinessRuleViolationException("Solo se puede marcar como no leída una notificación enviada");
        }
        this.read = false;
        this.updatedAt = at;
    }

    public void cancel(Instant at) {
        if (status != NotificationStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede cancelar una notificación PENDING");
        }
        this.status = NotificationStatus.CANCELLED;
        this.updatedAt = at;
    }

    private void requireTransition(NotificationStatus target, String operacion) {
        boolean valid = switch (target) {
            case PROCESSING -> status == NotificationStatus.PENDING;
            case SENT -> status == NotificationStatus.PROCESSING;
            default -> false;
        };
        if (!valid) {
            throw new BusinessRuleViolationException(
                    "No se puede " + operacion + " una notificación en estado " + status);
        }
        this.status = target;
    }
}