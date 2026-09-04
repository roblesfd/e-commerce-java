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

    /**
     * Crea una orden en estado {@code PENDING} con su primer estado en el historial.
     *
     * @param id              identificador de la orden.
     * @param customerId      cliente.
     * @param items           items con snapshots históricos.
     * @param addressId       identificador de la dirección de envío.
     * @param addressSnapshot copia de la dirección de envío.
     * @param couponCode      cupón aplicado (opcional).
     * @param subtotal        subtotal.
     * @param discounts       descuentos aplicados.
     * @param total           total.
     * @param at              momento de creación.
     * @return la orden creada.
     * @throws BusinessRuleViolationException si los totales son inconsistentes.
     */
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
    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     *
     * @return la orden reconstruida.
     * @throws BusinessRuleViolationException si no tiene items o los totales son inconsistentes.
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

    /**
     * @return los items de la orden como lista inmutable.
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @return el historial de estados como lista inmutable.
     */
    public List<OrderStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    /**
     * Confirma la orden.
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
    public void confirm(Instant at) {
        if (status != OrderStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede confirmar una orden PENDING");
        }
        transition(OrderStatus.CONFIRMED, at);
    }

    /**
     * Procesa la orden.
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
    public void process(Instant at) {
        requireTransition(OrderStatus.PROCESSING, "procesar", at);
    }

    /**
     * Marca la orden como lista para envío.
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
    public void markReadyToShip(Instant at) {
        requireTransition(OrderStatus.READY_TO_SHIP, "marcar como lista para envío", at);
    }

    /**
     * Marca la orden como enviada.
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
    public void markShipped(Instant at) {
        requireTransition(OrderStatus.SHIPPED, "marcar como enviada", at);
    }

    /**
     * Marca la orden como entregada.
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
    public void markDelivered(Instant at) {
        requireTransition(OrderStatus.DELIVERED, "marcar como entregada", at);
    }

    /**
     * Cancela la orden (solo {@code PENDING} o {@code CONFIRMED}).
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si la orden no está {@code PENDING} o {@code CONFIRMED}.
     */
    public void cancel(Instant at) {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("Solo se puede cancelar una orden PENDING o CONFIRMED");
        }
        transition(OrderStatus.CANCELLED, at);
    }

    /**
     * Valida y aplica una transición de estado.
     *
     * @param target    estado destino.
     * @param operacion operación en curso.
     * @param at        momento de la transición.
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
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

    /**
     * Aplica la transición y registra el nuevo estado en el historial.
     *
     * @param target estado destino.
     * @param at     momento de la transición.
     */
    private void transition(OrderStatus target, Instant at) {
        this.status = target;
        statusHistory.add(new OrderStatusEntry(target, at));
    }

    /**
     * Valida que los totales sean consistentes ({@code subtotal = discounts + total}).
     *
     * @param order orden a validar.
     * @throws BusinessRuleViolationException si los totales son inconsistentes.
     */
    private static void requireConsistentTotals(Order order) {
        if (!order.subtotal.equals(order.discounts.add(order.total))) {
            throw new BusinessRuleViolationException("Los totales de la orden son inconsistentes");
        }
    }
}