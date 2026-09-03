package org.fernandodev.ecommerce.notifications.adapter.out.providers;

import java.util.UUID;
import java.util.function.Supplier;

import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

import io.github.resilience4j.retry.Retry;

/**
 * Envuelve el enrutador de canales con Resilience4j (Retry) para manejar
 * fallos y timeouts del proveedor.
 */
public class ResilientNotificationProvider implements NotificationProviderPort {

    private final NotificationProviderPort delegate;
    private final Retry retry;

    public ResilientNotificationProvider(NotificationProviderPort delegate, Retry retry) {
        this.delegate = delegate;
        this.retry = retry;
    }

    @Override
    public DeliveryResult send(NotificationChannel channel, UUID customerId, String title, String body) {
        Supplier<DeliveryResult> decorated = Retry.decorateSupplier(retry,
                () -> delegate.send(channel, customerId, title, body));
        return decorated.get();
    }
}