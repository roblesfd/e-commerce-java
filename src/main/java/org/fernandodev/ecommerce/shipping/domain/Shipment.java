package org.fernandodev.ecommerce.shipping.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatusEntry;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa el envío de una orden y su ciclo de vida.
 * <p>
 * La integración con el contexto Order se realiza por puertos de aplicación
 * (al marcar shipped/delivered se actualiza el estado de la orden), no
 * modificando directamente su modelo de persistencia (spec §30).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Shipment {

    private ShipmentId id;
    private OrderId orderId;
    private UUID customerId;
    private ShippingMethodId shippingMethodId;
    private UUID addressId;
    private String addressSnapshot;
    private String trackingNumber;
    private Money cost;
    private ShipmentStatus status;
    @Getter(AccessLevel.NONE)
    private List<ShipmentStatusEntry> statusHistory;
    private Instant createdAt;
    private Instant updatedAt;

    public static Shipment create(
            ShipmentId id,
            OrderId orderId,
            UUID customerId,
            ShippingMethodId shippingMethodId,
            UUID addressId,
            String addressSnapshot,
            String trackingNumber,
            Money cost,
            Instant at
    ) {
        Shipment shipment = new Shipment();
        shipment.id = Objects.requireNonNull(id, "id no puede ser null");
        shipment.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        shipment.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        shipment.shippingMethodId = Objects.requireNonNull(shippingMethodId, "shippingMethodId no puede ser null");
        shipment.addressId = addressId;
        shipment.addressSnapshot = addressSnapshot;
        shipment.trackingNumber = Objects.requireNonNull(trackingNumber, "trackingNumber no puede ser null");
        shipment.cost = Objects.requireNonNull(cost, "cost no puede ser null");
        shipment.status = ShipmentStatus.PENDING;
        shipment.statusHistory = new ArrayList<>(List.of(new ShipmentStatusEntry(ShipmentStatus.PENDING, at)));
        shipment.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        shipment.updatedAt = at;
        return shipment;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Shipment crear(
            ShipmentId id,
            OrderId orderId,
            UUID customerId,
            ShippingMethodId shippingMethodId,
            UUID addressId,
            String addressSnapshot,
            String trackingNumber,
            Money cost,
            ShipmentStatus status,
            List<ShipmentStatusEntry> statusHistory,
            Instant createdAt,
            Instant updatedAt
    ) {
        Shipment shipment = new Shipment();
        shipment.id = Objects.requireNonNull(id, "id no puede ser null");
        shipment.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        shipment.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        shipment.shippingMethodId = Objects.requireNonNull(shippingMethodId, "shippingMethodId no puede ser null");
        shipment.addressId = addressId;
        shipment.addressSnapshot = addressSnapshot;
        shipment.trackingNumber = trackingNumber;
        shipment.cost = Objects.requireNonNull(cost, "cost no puede ser null");
        shipment.status = Objects.requireNonNull(status, "status no puede ser null");
        shipment.statusHistory = new ArrayList<>(statusHistory == null ? List.of() : statusHistory);
        shipment.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        shipment.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return shipment;
    }

    public List<ShipmentStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public void markPrepared(Instant at) {
        transition(ShipmentStatus.PREPARING, at);
    }

    public void markShipped(Instant at) {
        transition(ShipmentStatus.SHIPPED, at);
    }

    public void markInTransit(Instant at) {
        transition(ShipmentStatus.IN_TRANSIT, at);
    }

    public void markOutForDelivery(Instant at) {
        transition(ShipmentStatus.OUT_FOR_DELIVERY, at);
    }

    public void markDelivered(Instant at) {
        transition(ShipmentStatus.DELIVERED, at);
    }

    public void handleFailure(Instant at) {
        if (status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("No se puede marcar como fallido un envío " + status);
        }
        transition(ShipmentStatus.FAILED, at);
    }

    public void cancel(Instant at) {
        if (status != ShipmentStatus.PENDING && status != ShipmentStatus.PREPARING) {
            throw new BusinessRuleViolationException("Solo se puede cancelar un envío PENDING o PREPARING");
        }
        transition(ShipmentStatus.CANCELLED, at);
    }

    public void recordUnknownState(String detail, Instant at) {
        this.updatedAt = at;
    }

    public boolean isTerminal() {
        return status == ShipmentStatus.DELIVERED
                || status == ShipmentStatus.FAILED
                || status == ShipmentStatus.CANCELLED;
    }

    private void transition(ShipmentStatus target, Instant at) {
        if (!canTransitionTo(target)) {
            throw new BusinessRuleViolationException(
                    "Transición inválida de envío: " + status + " → " + target);
        }
        this.status = target;
        this.updatedAt = at;
        statusHistory.add(new ShipmentStatusEntry(target, at));
    }

    private boolean canTransitionTo(ShipmentStatus target) {
        return switch (target) {
            case PREPARING -> status == ShipmentStatus.PENDING;
            case SHIPPED -> status == ShipmentStatus.PREPARING;
            case IN_TRANSIT -> status == ShipmentStatus.SHIPPED;
            case OUT_FOR_DELIVERY -> status == ShipmentStatus.IN_TRANSIT;
            case DELIVERED -> status == ShipmentStatus.OUT_FOR_DELIVERY;
            case FAILED -> status != ShipmentStatus.DELIVERED && status != ShipmentStatus.CANCELLED;
            case CANCELLED -> status == ShipmentStatus.PENDING || status == ShipmentStatus.PREPARING;
            default -> false;
        };
    }
}