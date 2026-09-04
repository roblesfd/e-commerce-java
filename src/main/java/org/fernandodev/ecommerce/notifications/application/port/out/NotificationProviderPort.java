package org.fernandodev.ecommerce.notifications.application.port.out;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

/**
 * Puerto hacia el proveedor de notificaciones.
 */
public interface NotificationProviderPort {

    /**
     * Envía la notificación por el canal indicado.
     *
     * @param channel    canal de entrega.
     * @param customerId destinatario.
     * @param title      título.
     * @param body       cuerpo.
     * @return el resultado de la entrega.
     */
    DeliveryResult send(NotificationChannel channel, java.util.UUID customerId, String title, String body);

    record DeliveryResult(boolean success) {
        /**
         * @return un resultado de entrega exitoso.
         */
    public static DeliveryResult ok() {
            return new DeliveryResult(true);
        }

        /**
         * @return un resultado de entrega fallido.
         */
    public static DeliveryResult fail() {
            return new DeliveryResult(false);
        }
    }
}