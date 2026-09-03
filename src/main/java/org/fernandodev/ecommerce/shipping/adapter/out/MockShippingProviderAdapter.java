package org.fernandodev.ecommerce.shipping.adapter.out;

import java.util.UUID;

import org.fernandodev.ecommerce.shipping.application.port.out.ShippingProviderPort;
import org.springframework.stereotype.Component;

/**
 * Mock de proveedor externo de envíos.
 * <p>
 * Genera un número de tracking determinístico. Los carriers reales lo
 * sustituirán con adapters.
 * </p>
 */
@Component
public class MockShippingProviderAdapter implements ShippingProviderPort {

    @Override
    public String createShipment(String carrier, String addressSnapshot) {
        return carrier + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}