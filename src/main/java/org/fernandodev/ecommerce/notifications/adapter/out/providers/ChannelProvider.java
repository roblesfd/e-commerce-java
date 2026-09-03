package org.fernandodev.ecommerce.notifications.adapter.out.providers;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

/**
 * Proveedor de un canal concreto. Los adaptadores de infraestructura lo
 * implementan para cada canal externo.
 */
public interface ChannelProvider {

    NotificationChannel channel();

    NotificationProviderPort.DeliveryResult send(UUID customerId, String title, String body);
}