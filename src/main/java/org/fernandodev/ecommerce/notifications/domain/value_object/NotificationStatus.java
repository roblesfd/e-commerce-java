package org.fernandodev.ecommerce.notifications.domain.value_object;

/**
 * Estado de entrega de una notificación.
 */
public enum NotificationStatus {

    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    CANCELLED
}