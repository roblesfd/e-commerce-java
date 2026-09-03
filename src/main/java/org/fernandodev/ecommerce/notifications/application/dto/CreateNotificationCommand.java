package org.fernandodev.ecommerce.notifications.application.dto;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

/**
 * Datos para crear una notificación (cliente e identificación vía puerto de entrada).
 */
public record CreateNotificationCommand(NotificationChannel channel, String title, String body, String dedupKey) {
}