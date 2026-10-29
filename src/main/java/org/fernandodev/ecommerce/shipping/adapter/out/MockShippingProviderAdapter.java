package org.fernandodev.ecommerce.shipping.adapter.out;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentTrackingResult;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingProviderPort;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingQuoteRequest;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingQuoteResult;
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

    @Override
    public ShippingQuoteResult quote(ShippingQuoteRequest request) {
        return new ShippingQuoteResult(List.of(
                new ShippingQuoteResult.ShippingOption(
                        "DHL",
                        "Estándar",
                        new BigDecimal("50.00"),
                        "USD",
                        Duration.ofDays(5)
                ),
                new ShippingQuoteResult.ShippingOption(
                        "DHL",
                        "Exprés",
                        new BigDecimal("100.00"),
                        "USD",
                        Duration.ofDays(2)
                )
        ));
    }

    @Override
    public ShipmentTrackingResult trackShipment(String providerShipmentId) {
        return new ShipmentTrackingResult(
                providerShipmentId,
                providerShipmentId,
                "in_transit",
                Instant.now(),
                null
        );
    }

    @Override
    public void cancelShipment(String providerShipmentId) {
        // El mock no necesita ejecutar una cancelación real.
    }
}
