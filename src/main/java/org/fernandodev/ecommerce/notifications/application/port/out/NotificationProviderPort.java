package org.fernandodev.ecommerce.notifications.application.port.out;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

/**
 * Puerto hacia el proveedor de notificaciones.
 */
public interface NotificationProviderPort {

    DeliveryResult send(NotificationChannel channel, java.util.UUID customerId, String title, String body);

    record DeliveryResult(boolean success) {
        public static DeliveryResult ok() {
            return new DeliveryResult(true);
        }

        public static DeliveryResult fail() {
            return new DeliveryResult(false);
        }
    }
}