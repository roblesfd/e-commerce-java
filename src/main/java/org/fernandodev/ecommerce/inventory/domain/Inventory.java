package org.fernandodev.ecommerce.inventory.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.inventory.domain.value_object.ReservationStatus;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovement;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovementType;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservation;
import org.fernandodev.ecommerce.inventory.domain.value_object.StockReservationId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Inventory que representa el stock de un producto
 * o de una variante de producto.
 * <p>
 * Concurrencia: las operaciones que mutan stock/reservas se ejecutan con
 * {@code PESSIMISTIC_WRITE} sobre la fila (puerto de salida) dentro de una
 * transacción que abarca lectura + escritura, lo que previene el overselling
 * bajo reservas concurrentes (spec §24).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Inventory {

    private InventoryId id;
    private ProductId productId;
    private ProductVariantId variantId;
    private int onHand;
    private int reserved;
    private int minimumStock;
    @Getter(AccessLevel.NONE)
    private List<StockMovement> movements;
    @Getter(AccessLevel.NONE)
    private List<StockReservation> reservations;

    /**
     * Define un inventario con stock inicial.
     */
    public static Inventory define(InventoryId id, ProductId productId, ProductVariantId variantId,
                                   int onHand, int minimumStock) {
        return crear(id, productId, variantId, onHand, 0, minimumStock, List.of(), List.of());
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Inventory crear(
            InventoryId id,
            ProductId productId,
            ProductVariantId variantId,
            int onHand,
            int reserved,
            int minimumStock,
            List<StockMovement> movements,
            List<StockReservation> reservations
    ) {
        Inventory inventory = new Inventory();
        inventory.id = Objects.requireNonNull(id, "id no puede ser null");
        inventory.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        inventory.variantId = variantId;
        requireNonNegative(onHand, "onHand");
        requireNonNegative(reserved, "reserved");
        requireNonNegative(minimumStock, "minimumStock");
        inventory.onHand = onHand;
        inventory.reserved = reserved;
        inventory.minimumStock = minimumStock;
        inventory.movements = new ArrayList<>(movements == null ? List.of() : movements);
        inventory.reservations = new ArrayList<>(reservations == null ? List.of() : reservations);
        return inventory;
    }

    public Optional<ProductVariantId> getVariantId() {
        return Optional.ofNullable(variantId);
    }

    /**
     * Stock disponible para la venta/reserva.
     */
    public int availableQuantity() {
        return onHand - reserved;
    }

    public boolean isLowStock() {
        return availableQuantity() <= minimumStock;
    }

    public List<StockMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

    public List<StockReservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void increase(int quantity, String reason, Instant at) {
        requirePositive(quantity, "quantity");
        onHand += quantity;
        registerMovement(StockMovementType.INCREASE, quantity, reason, at);
    }

    public void decrease(int quantity, String reason, Instant at) {
        requirePositive(quantity, "quantity");
        if (quantity > onHand) {
            throw new BusinessRuleViolationException("No hay suficiente stock para disminuir " + quantity);
        }
        if (availableQuantity() - quantity < 0) {
            throw new BusinessRuleViolationException("No se puede reducir el stock por debajo de lo reservado");
        }
        onHand -= quantity;
        registerMovement(StockMovementType.DECREASE, -quantity, reason, at);
    }

    public void adjust(int targetQuantity, String reason, Instant at) {
        requireNonNegative(targetQuantity, "targetQuantity");
        if (targetQuantity < reserved) {
            throw new BusinessRuleViolationException("El stock no puede quedar por debajo de lo reservado");
        }
        int delta = targetQuantity - onHand;
        onHand = targetQuantity;
        registerMovement(StockMovementType.ADJUST, delta, reason, at);
    }

    public void setMinimumStock(int minimumStock) {
        requireNonNegative(minimumStock, "minimumStock");
        this.minimumStock = minimumStock;
    }

    /**
     * Reserva stock validando la disponibilidad (previene overselling).
     *
     * @return la reserva creada en estado {@code ACTIVE}.
     */
    public StockReservation reserve(int quantity, Instant expiresAt, Instant at) {
        requirePositive(quantity, "quantity");
        Objects.requireNonNull(expiresAt, "expiresAt no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
        if (!expiresAt.isAfter(at)) {
            throw new BusinessRuleViolationException("expiresAt debe ser posterior al momento actual");
        }
        if (quantity > availableQuantity()) {
            throw new BusinessRuleViolationException(
                    "Stock insuficiente para reservar " + quantity + " (disponible " + availableQuantity() + ")");
        }
        StockReservation reservation = new StockReservation(
                StockReservationId.generate(), quantity, ReservationStatus.ACTIVE, at, expiresAt);
        reservations.add(reservation);
        reserved += quantity;
        registerMovement(StockMovementType.RESERVE, quantity, null, at);
        return reservation;
    }

    public void confirmReservation(StockReservationId id, Instant at) {
        StockReservation reservation = requireActive(id, "confirmar");
        replace(reservation.withStatus(ReservationStatus.CONFIRMED));
        reserved -= reservation.quantity();
        onHand -= reservation.quantity();
        registerMovement(StockMovementType.CONFIRM, -reservation.quantity(), null, at);
    }

    public void releaseReservation(StockReservationId id, Instant at) {
        StockReservation reservation = requireActive(id, "liberar");
        replace(reservation.withStatus(ReservationStatus.RELEASED));
        reserved -= reservation.quantity();
        registerMovement(StockMovementType.RELEASE, -reservation.quantity(), null, at);
    }

    public void expireReservation(StockReservationId id, Instant at) {
        StockReservation reservation = requireActive(id, "expirar");
        replace(reservation.withStatus(ReservationStatus.EXPIRED));
        reserved -= reservation.quantity();
        registerMovement(StockMovementType.EXPIRE, -reservation.quantity(), null, at);
    }

    private StockReservation requireActive(StockReservationId id, String operacion) {
        StockReservation reservation = reservations.stream()
                .filter(r -> r.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + id));
        if (!reservation.isActive()) {
            throw new BusinessRuleViolationException(
                    "Solo se puede " + operacion + " una reserva activa (estado actual: " + reservation.status() + ")");
        }
        return reservation;
    }

    private void replace(StockReservation updated) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).id().equals(updated.id())) {
                reservations.set(i, updated);
                return;
            }
        }
    }

    private void registerMovement(StockMovementType type, int quantity, String reason, Instant at) {
        movements.add(new StockMovement(type, quantity, reason, at));
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new BusinessRuleViolationException(name + " debe ser positivo");
        }
    }

    private static void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new BusinessRuleViolationException(name + " no puede ser negativo");
        }
    }
}