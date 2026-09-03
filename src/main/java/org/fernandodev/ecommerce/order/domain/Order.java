package org.fernandodev.ecommerce.order.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItem;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatusEntry;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Order Management.
 * <p>
 * Preserva snapshots históricos de producto, precio y descuento (spec §28): la
 * orden no depende del estado mutable del catálogo o del pricing.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    private OrderId id;
    private UUID customerId;
    @Getter(AccessLevel.NONE)
    private List<OrderItem> items;
    private OrderStatus status;
    private UUID addressId;
    private String addressSnapshot;
    private String couponCode;
    private Money subtotal;
    private Money discounts;
    private Money total;
    @Getter(AccessLevel.NONE)
    private List<OrderStatusEntry> statusHistory;
    private Instant createdAt;

    public static Order create(
            OrderId id,
            UUID customerId,
            List<OrderItem> items,
            UUID addressId,
            String addressSnapshot,
            String couponCode,
            Money subtotal,
            Money discounts,
            Money total,
            Instant at
    ) {
        Order order = crear(id, customerId, items, OrderStatus.PENDING, addressId, addressSnapshot,
                couponCode, subtotal, discounts, total, List.of(new OrderStatusEntry(OrderStatus.PENDING, at)), at);
        return order;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Order crear(
            OrderId id,
            UUID customerId,
            List<OrderItem> items,
            OrderStatus status,
            UUID addressId,
            String addressSnapshot,
            String couponCode,
            Money subtotal,
            Money discounts,
            Money total,
            List<OrderStatusEntry> statusHistory,
            Instant createdAt
    ) {
        Order order = new Order();
        order.id = Objects.requireNonNull(id, "id no puede ser null");
        order.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        order.items = new ArrayList<>(items == null ? List.of() : items);
        if (order.items.isEmpty()) {
            throw new BusinessRuleViolationException("La orden debe tener al menos un item");
        }
        order.status = Objects.requireNonNull(status, "status no puede ser null");
        order.addressId = addressId;
        order.addressSnapshot = addressSnapshot;
        order.couponCode = couponCode;
        order.subtotal = Objects.requireNonNull(subtotal, "subtotal no puede ser null");
        order.discounts = Objects.requireNonNull(discounts, "discounts no puede ser null");
        order.total = Objects.requireNonNull(total, "total no puede ser null");
        requireConsistentTotals(order);
        order.statusHistory = new ArrayList<>(statusHistory == null ? List.of() : statusHistory);
        order.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        return order;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<OrderStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public void confirm(Instant at) {
        if (status != OrderStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede confirmar una orden PENDING");
        }
        transition(OrderStatus.CONFIRMED, at);
    }

    public void process(Instant at) {
        requireTransition(OrderStatus.PROCESSING, "procesar", at);
    }

    public void markReadyToShip(Instant at) {
        requireTransition(OrderStatus.READY_TO_SHIP, "marcar como lista para envío", at);
    }

    public void markShipped(Instant at) {
        requireTransition(OrderStatus.SHIPPED, "marcar como enviada", at);
    }

    public void markDelivered(Instant at) {
        requireTransition(OrderStatus.DELIVERED, "marcar como entregada", at);
    }

    public void cancel(Instant at) {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("Solo se puede cancelar una orden PENDING o CONFIRMED");
        }
        transition(OrderStatus.CANCELLED, at);
    }

    private void requireTransition(OrderStatus target, String operacion, Instant at) {
        boolean valid = switch (target) {
            case PROCESSING -> status == OrderStatus.CONFIRMED;
            case READY_TO_SHIP -> status == OrderStatus.PROCESSING;
            case SHIPPED -> status == OrderStatus.READY_TO_SHIP;
            case DELIVERED -> status == OrderStatus.SHIPPED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessRuleViolationException(
                    "No se puede " + operacion + " una orden en estado " + status);
        }
        transition(target, at);
    }

    private void transition(OrderStatus target, Instant at) {
        this.status = target;
        statusHistory.add(new OrderStatusEntry(target, at));
    }

    private static void requireConsistentTotals(Order order) {
        if (!order.subtotal.equals(order.discounts.add(order.total))) {
            throw new BusinessRuleViolationException("Los totales de la orden son inconsistentes");
        }
    }
}