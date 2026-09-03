package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentRepository;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaShipmentRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private ShipmentRepository repository;

    @Autowired
    private org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository methods;

    @Test
    void guardaYRecuperaEnvioConHistorial() {
        ShippingMethodId methodId = guardarMetodo();
        OrderId orderId = OrderId.generate();
        UUID customerId = UUID.randomUUID();
        Shipment shipment = Shipment.create(ShipmentId.generate(), orderId, customerId, methodId,
                UUID.randomUUID(), "Calle 1, Madrid, España", "DHL-ABC123", usd(50), T1);
        shipment.markPrepared(T1);

        repository.save(shipment);
        Shipment loaded = repository.findById(shipment.getId()).orElseThrow();

        assertThat(loaded.getOrderId()).isEqualTo(orderId);
        assertThat(loaded.getCustomerId()).isEqualTo(customerId);
        assertThat(loaded.getTrackingNumber()).isEqualTo("DHL-ABC123");
        assertThat(loaded.getStatus()).isEqualTo(ShipmentStatus.PREPARING);
        assertThat(loaded.getCost()).isEqualTo(usd(50));
        assertThat(loaded.getStatusHistory()).hasSize(2);
    }

    @Test
    void findByOrderIdDevuelveElEnvio() {
        Shipment shipment = guardarEnvio();

        assertThat(repository.findByOrderId(shipment.getOrderId()).orElseThrow().getId())
                .isEqualTo(shipment.getId());
    }

    @Test
    void findByTrackingNumberDevuelveElEnvio() {
        Shipment shipment = guardarEnvio();

        assertThat(repository.findByTrackingNumber(shipment.getTrackingNumber()).orElseThrow().getId())
                .isEqualTo(shipment.getId());
    }

    @Test
    void findByCustomerIdDevuelveEnvios() {
        Shipment shipment = guardarEnvio();

        assertThat(repository.findByCustomerId(shipment.getCustomerId()))
                .extracting(Shipment::getId)
                .containsExactly(shipment.getId());
    }

    private Shipment guardarEnvio() {
        return repository.save(Shipment.create(ShipmentId.generate(), OrderId.generate(), UUID.randomUUID(),
                guardarMetodo(), UUID.randomUUID(), "Calle 1, Madrid, España",
                "DHL-" + UUID.randomUUID().toString().substring(0, 8), usd(50), T1));
    }

    private ShippingMethodId guardarMetodo() {
        return methods.save(ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL", usd(50))).getId();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}