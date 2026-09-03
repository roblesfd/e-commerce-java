package org.fernandodev.ecommerce.shipping.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.application.dto.CreateShipmentCommand;
import org.fernandodev.ecommerce.shipping.application.port.out.CustomerAddressProvider;
import org.fernandodev.ecommerce.shipping.application.port.out.OrderPort;
import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentRepository;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingProviderPort;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ShipmentServiceTest {

    private final FakeShipmentRepository shipments = new FakeShipmentRepository();
    private final FakeShippingMethodRepository methods = new FakeShippingMethodRepository();
    private final FakeShippingProviderPort provider = new FakeShippingProviderPort();
    private final FakeOrderPort orders = new FakeOrderPort();
    private final FakeCustomerAddressProvider addresses = new FakeCustomerAddressProvider();
    private final ShipmentService service = new ShipmentService(shipments, methods, provider, orders, addresses);

    @Test
    void createCreaEnvioParaOrdenConfirmada() {
        ShippingMethodId methodId = guardarMetodo();

        Shipment shipment = service.create(OrderId.generate(), new CreateShipmentCommand(methodId, UUID.randomUUID()));

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
        assertThat(shipment.getTrackingNumber()).isNotEmpty();
        assertThat(shipment.getCost()).isEqualTo(usd(50));
        assertThat(shipments.findById(shipment.getId())).contains(shipment);
    }

    @Test
    void createConOrdenNoConfirmadaLanzaViolacion() {
        orders.status = "PENDING";
        ShippingMethodId methodId = guardarMetodo();

        assertThatThrownBy(() -> service.create(OrderId.generate(),
                new CreateShipmentCommand(methodId, UUID.randomUUID())))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createConMetodoInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.create(OrderId.generate(),
                new CreateShipmentCommand(ShippingMethodId.generate(), UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createConMetodoInactivoLanzaViolacion() {
        ShippingMethodId methodId = guardarMetodo();
        methods.byId.get(methodId).deactivate();

        assertThatThrownBy(() -> service.create(OrderId.generate(),
                new CreateShipmentCommand(methodId, UUID.randomUUID())))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void markShippedActualizaLaOrden() {
        Shipment shipment = crearEnvio();

        Shipment shipped = service.markPrepared(shipment.getId());
        shipped = service.markShipped(shipment.getId());

        assertThat(shipped.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);
        assertThat(orders.shipped).contains(shipment.getOrderId());
    }

    @Test
    void markDeliveredActualizaLaOrden() {
        Shipment shipment = crearEnvio();
        servicioCompleto(shipment);

        Shipment delivered = service.markDelivered(shipment.getId());

        assertThat(delivered.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(orders.delivered).contains(shipment.getOrderId());
    }

    @Test
    void cancelCancelaElEnvio() {
        Shipment shipment = crearEnvio();

        Shipment cancelled = service.cancel(shipment.getId());

        assertThat(cancelled.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    void listByCustomerDevuelveEnvios() {
        Shipment shipment = crearEnvio();

        assertThat(service.listByCustomer(shipment.getCustomerId()))
                .extracting(Shipment::getId)
                .containsExactly(shipment.getId());
    }

    @Test
    void getByOrderDevuelveElEnvio() {
        Shipment shipment = crearEnvio();

        assertThat(service.getByOrder(shipment.getOrderId())).contains(shipment);
    }

    @Test
    void calculateCostDevuelveElCosto() {
        ShippingMethodId methodId = guardarMetodo();

        assertThat(service.calculateCost(methodId)).isEqualTo(usd(50));
    }

    @Test
    void processWebhookDeliveredConfirmaYActualizaOrden() {
        Shipment shipment = crearEnvio();
        servicioCompleto(shipment);

        Shipment processed = service.processWebhook(shipment.getTrackingNumber(), "delivered");

        assertThat(processed.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(orders.delivered).contains(shipment.getOrderId());
    }

    @Test
    void processWebhookEstadoDesconocidoSeRegistraSinCambiarEstado() {
        Shipment shipment = crearEnvio();

        Shipment processed = service.processWebhook(shipment.getTrackingNumber(), "estado-raro");

        assertThat(processed.getStatus()).isEqualTo(ShipmentStatus.PENDING);
    }

    private ShippingMethodId guardarMetodo() {
        return methods.save(ShippingMethod.create(ShippingMethodId.generate(), "Estándar", "DHL", usd(50))).getId();
    }

    private Shipment crearEnvio() {
        return service.create(OrderId.generate(), new CreateShipmentCommand(guardarMetodo(), UUID.randomUUID()));
    }

    private void servicioCompleto(Shipment shipment) {
        service.markPrepared(shipment.getId());
        service.markShipped(shipment.getId());
        service.markInTransit(shipment.getId());
        service.markOutForDelivery(shipment.getId());
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeShipmentRepository implements ShipmentRepository {

        private final Map<ShipmentId, Shipment> byId = new HashMap<>();

        @Override
        public Shipment save(Shipment shipment) {
            byId.put(shipment.getId(), shipment);
            return shipment;
        }

        @Override
        public Optional<Shipment> findById(ShipmentId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Shipment> findByOrderId(OrderId orderId) {
            return byId.values().stream().filter(s -> s.getOrderId().equals(orderId)).findFirst();
        }

        @Override
        public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
            return byId.values().stream().filter(s -> trackingNumber.equals(s.getTrackingNumber())).findFirst();
        }

        @Override
        public List<Shipment> findByCustomerId(UUID customerId) {
            return byId.values().stream().filter(s -> s.getCustomerId().equals(customerId)).toList();
        }
    }

    private static final class FakeShippingMethodRepository implements ShippingMethodRepository {

        private final Map<ShippingMethodId, ShippingMethod> byId = new HashMap<>();

        @Override
        public ShippingMethod save(ShippingMethod method) {
            byId.put(method.getId(), method);
            return method;
        }

        @Override
        public Optional<ShippingMethod> findById(ShippingMethodId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<ShippingMethod> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public boolean existsById(ShippingMethodId id) {
            return byId.containsKey(id);
        }
    }

    private static final class FakeShippingProviderPort implements ShippingProviderPort {
        @Override
        public String createShipment(String carrier, String addressSnapshot) {
            return "SHIP-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    private static final class FakeOrderPort implements OrderPort {
        private String status = "CONFIRMED";
        private final List<OrderId> shipped = new java.util.ArrayList<>();
        private final List<OrderId> delivered = new java.util.ArrayList<>();

        @Override
        public OrderInfo getOrder(OrderId orderId) {
            return new OrderInfo(UUID.randomUUID(), status);
        }

        @Override
        public void markShipped(OrderId orderId) {
            shipped.add(orderId);
        }

        @Override
        public void markDelivered(OrderId orderId) {
            delivered.add(orderId);
        }
    }

    private static final class FakeCustomerAddressProvider implements CustomerAddressProvider {
        @Override
        public String addressSnapshotOf(UUID customerId, UUID addressId) {
            return "Calle 1, Madrid, España";
        }
    }
}