package org.fernandodev.ecommerce.shipping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class ShipmentTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaEnvioPending() {
        Shipment shipment = envio();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
        assertThat(shipment.getTrackingNumber()).isNotEmpty();
        assertThat(shipment.getCost()).isEqualTo(usd(50));
        assertThat(shipment.getStatusHistory()).hasSize(1);
    }

    @Test
    void cicloDeVidaCompletoHastaEntregado() {
        Shipment shipment = envio();

        shipment.markPrepared(T1);
        shipment.markShipped(T1);
        shipment.markInTransit(T1);
        shipment.markOutForDelivery(T1);
        shipment.markDelivered(T1);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(shipment.isTerminal()).isTrue();
        assertThat(shipment.getStatusHistory()).hasSize(6);
    }

    @Test
    void transicionesInvalidasLanzanExcepcion() {
        Shipment shipment = envio();

        assertThatThrownBy(() -> shipment.markDelivered(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void handleFailureDesdeEnTransito() {
        Shipment shipment = envio();
        shipment.markPrepared(T1);
        shipment.markShipped(T1);
        shipment.markInTransit(T1);

        shipment.handleFailure(T1);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.FAILED);
    }

    @Test
    void cancelarEnvioPendingEsValido() {
        Shipment shipment = envio();

        shipment.cancel(T1);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    void cancelarEnvioEntregadoLanzaExcepcion() {
        Shipment shipment = envio();
        shipment.markPrepared(T1);
        shipment.markShipped(T1);
        shipment.markInTransit(T1);
        shipment.markOutForDelivery(T1);
        shipment.markDelivered(T1);

        assertThatThrownBy(() -> shipment.cancel(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Shipment envio() {
        return Shipment.create(ShipmentId.generate(), OrderId.generate(), UUID.randomUUID(),
                ShippingMethodId.generate(), UUID.randomUUID(), "Calle 1, Madrid, España",
                "SHIP-" + UUID.randomUUID().toString().substring(0, 8), usd(50), T1);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}