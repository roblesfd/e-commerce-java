package org.fernandodev.ecommerce.notifications.adapter.out.providers;

import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Proveedor simulado de push. Sustituible por una integración real.
 */
@Component
@Slf4j
public class MockPushProvider implements ChannelProvider {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public NotificationProviderPort.DeliveryResult send(UUID customerId, String title, String body) {
        log.info("Push simulado para {}: {}", customerId, title);
        return NotificationProviderPort.DeliveryResult.ok();
    }
}