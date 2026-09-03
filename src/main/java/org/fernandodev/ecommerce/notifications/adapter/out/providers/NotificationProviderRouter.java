package org.fernandodev.ecommerce.notifications.adapter.out.providers;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;

/**
 * Enruta la entrega al proveedor del canal. IN_APP se considera entregado al
 * persistirse (no hay proveedor externo).
 */
public class NotificationProviderRouter implements NotificationProviderPort {

    private final List<ChannelProvider> providers;

    public NotificationProviderRouter(List<ChannelProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public DeliveryResult send(NotificationChannel channel, UUID customerId, String title, String body) {
        if (channel == NotificationChannel.IN_APP) {
            return DeliveryResult.ok();
        }
        return providers.stream()
                .filter(provider -> provider.channel() == channel)
                .findFirst()
                .map(provider -> provider.send(customerId, title, body))
                .orElse(DeliveryResult.fail());
    }
}