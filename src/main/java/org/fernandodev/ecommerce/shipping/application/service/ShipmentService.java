package org.fernandodev.ecommerce.shipping.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.application.dto.CreateShipmentCommand;
import org.fernandodev.ecommerce.shipping.application.port.in.ShipmentUseCases;
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
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de envíos.
 * <p>
 * Orquesta la creación del envío (validando la orden confirmada y obteniendo el
 * tracking del proveedor) y avanza el ciclo de vida actualizando el estado de la
 * orden vía el puerto (spec §30: comunicación entre Shipping y Order por
 * interfaces de aplicación, sin tocar su modelo de persistencia).
 * </p>
 */
@RequiredArgsConstructor
public class ShipmentService implements ShipmentUseCases {

    private static final String CONFIRMED = "CONFIRMED";

    private final ShipmentRepository shipments;
    private final ShippingMethodRepository methods;
    private final ShippingProviderPort provider;
    private final OrderPort orders;
    private final CustomerAddressProvider addresses;

    @Override
    @Transactional
    public Shipment create(OrderId orderId, CreateShipmentCommand command) {
        OrderPort.OrderInfo order = orders.getOrder(orderId);
        if (!CONFIRMED.equals(order.status())) {
            throw new BusinessRuleViolationException("El envío solo puede crearse para una orden CONFIRMED");
        }
        ShippingMethod method = methods.findById(command.shippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Método de envío no encontrado: " + command.shippingMethodId()));
        if (!method.isActive()) {
            throw new BusinessRuleViolationException("El método de envío no está activo");
        }
        String addressSnapshot = addresses.addressSnapshotOf(order.customerId(), command.addressId());
        String trackingNumber = provider.createShipment(method.getCarrier(), addressSnapshot);

        Shipment shipment = Shipment.create(
                ShipmentId.generate(),
                orderId,
                order.customerId(),
                method.getId(),
                command.addressId(),
                addressSnapshot,
                trackingNumber,
                method.getCost(),
                Instant.now());
        return shipments.save(shipment);
    }

    @Override
    public Shipment getById(ShipmentId id) {
        return getExisting(id);
    }

    @Override
    public List<Shipment> listByCustomer(UUID customerId) {
        return shipments.findByCustomerId(customerId);
    }

    @Override
    public Optional<Shipment> getByOrder(OrderId orderId) {
        return shipments.findByOrderId(orderId);
    }

    @Override
    public Money calculateCost(ShippingMethodId methodId) {
        ShippingMethod method = methods.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Método de envío no encontrado: " + methodId));
        return method.getCost();
    }

    @Override
    @Transactional
    public Shipment markPrepared(ShipmentId id) {
        return transition(id, Transition.PREPARED);
    }

    @Override
    @Transactional
    public Shipment markShipped(ShipmentId id) {
        return transition(id, Transition.SHIPPED);
    }

    @Override
    @Transactional
    public Shipment markInTransit(ShipmentId id) {
        return transition(id, Transition.IN_TRANSIT);
    }

    @Override
    @Transactional
    public Shipment markOutForDelivery(ShipmentId id) {
        return transition(id, Transition.OUT_FOR_DELIVERY);
    }

    @Override
    @Transactional
    public Shipment markDelivered(ShipmentId id) {
        return transition(id, Transition.DELIVERED);
    }

    @Override
    @Transactional
    public Shipment handleFailure(ShipmentId id) {
        Shipment shipment = getExisting(id);
        shipment.handleFailure(Instant.now());
        return shipments.save(shipment);
    }

    @Override
    @Transactional
    public Shipment cancel(ShipmentId id) {
        Shipment shipment = getExisting(id);
        shipment.cancel(Instant.now());
        return shipments.save(shipment);
    }

    @Override
    @Transactional
    public Shipment processWebhook(String trackingNumber, String providerStatus) {
        Shipment shipment = shipments.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado: " + trackingNumber));
        applyWebhook(shipment, providerStatus);
        return shipments.save(shipment);
    }

    private void applyWebhook(Shipment shipment, String providerStatus) {
        Transition transition = switch (providerStatus.toLowerCase()) {
            case "prepared" -> Transition.PREPARED;
            case "shipped" -> Transition.SHIPPED;
            case "in_transit", "in-transit" -> Transition.IN_TRANSIT;
            case "out_for_delivery", "out-for-delivery" -> Transition.OUT_FOR_DELIVERY;
            case "delivered" -> Transition.DELIVERED;
            case "failed" -> Transition.FAILED;
            case "cancelled" -> Transition.CANCELLED;
            default -> null;
        };
        if (transition == null) {
            shipment.recordUnknownState("estado de proveedor desconocido: " + providerStatus, Instant.now());
            return;
        }
        applyTransition(shipment, transition);
    }

    private Shipment transition(ShipmentId id, Transition transition) {
        Shipment shipment = getExisting(id);
        applyTransition(shipment, transition);
        return shipments.save(shipment);
    }

    private void applyTransition(Shipment shipment, Transition transition) {
        Instant now = Instant.now();
        ShipmentStatus target = transition.target();
        if (shipment.getStatus() == target) {
            return;
        }
        switch (transition) {
            case PREPARED -> shipment.markPrepared(now);
            case SHIPPED -> {
                shipment.markShipped(now);
                orders.markShipped(shipment.getOrderId());
            }
            case IN_TRANSIT -> shipment.markInTransit(now);
            case OUT_FOR_DELIVERY -> shipment.markOutForDelivery(now);
            case DELIVERED -> {
                shipment.markDelivered(now);
                orders.markDelivered(shipment.getOrderId());
            }
            case FAILED -> shipment.handleFailure(now);
            case CANCELLED -> shipment.cancel(now);
        }
    }

    private Shipment getExisting(ShipmentId id) {
        return shipments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado: " + id));
    }

    private enum Transition {
        PREPARED(ShipmentStatus.PREPARING),
        SHIPPED(ShipmentStatus.SHIPPED),
        IN_TRANSIT(ShipmentStatus.IN_TRANSIT),
        OUT_FOR_DELIVERY(ShipmentStatus.OUT_FOR_DELIVERY),
        DELIVERED(ShipmentStatus.DELIVERED),
        FAILED(ShipmentStatus.FAILED),
        CANCELLED(ShipmentStatus.CANCELLED);

        private final ShipmentStatus target;

        Transition(ShipmentStatus target) {
            this.target = target;
        }

        ShipmentStatus target() {
            return target;
        }
    }
}