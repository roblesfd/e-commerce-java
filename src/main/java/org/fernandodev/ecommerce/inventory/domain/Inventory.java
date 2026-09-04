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
     *
     * @param id           identificador del inventario.
     * @param productId    producto.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @param onHand       stock físico inicial.
     * @param minimumStock umbral de stock mínimo.
     * @return el inventario definido.
     * @throws BusinessRuleViolationException si algún valor de stock es negativo.
     */
    public static Inventory define(InventoryId id, ProductId productId, ProductVariantId variantId,
                                   int onHand, int minimumStock) {
        return crear(id, productId, variantId, onHand, 0, minimumStock, List.of(), List.of());
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     *
     * @param id           identificador del inventario.
     * @param productId    producto.
     * @param variantId    variante (null si el stock es a nivel producto).
     * @param onHand       stock físico.
     * @param reserved     stock reservado.
     * @param minimumStock umbral de stock mínimo.
     * @param movements    movimientos de inventario.
     * @param reservations reservas de stock.
     * @return el inventario reconstruido.
     * @throws BusinessRuleViolationException si algún valor de stock es negativo.
     * @throws NullPointerException           si {@code id} o {@code productId} son {@code null}.
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

    /**
     * @return la variante, o {@code Optional.empty()} si el stock es a nivel producto.
     */
    public Optional<ProductVariantId> getVariantId() {
        return Optional.ofNullable(variantId);
    }

    /**
     * @return el stock disponible para la venta/reserva ({@code onHand − reserved}).
     */
    public int availableQuantity() {
        return onHand - reserved;
    }

    /**
     * @return {@code true} si el stock disponible está por debajo o igual al mínimo.
     */
    public boolean isLowStock() {
        return availableQuantity() <= minimumStock;
    }

    /**
     * @return los movimientos de inventario como lista inmutable.
     */
    public List<StockMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

    /**
     * @return las reservas de stock como lista inmutable.
     */
    public List<StockReservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    /**
     * Incrementa el stock físico y registra el movimiento.
     *
     * @param quantity cantidad a incrementar.
     * @param reason    motivo del movimiento (opcional).
     * @param at        momento del movimiento.
     * @throws BusinessRuleViolationException si {@code quantity} no es positiva.
     */
    public void increase(int quantity, String reason, Instant at) {
        requirePositive(quantity, "quantity");
        onHand += quantity;
        registerMovement(StockMovementType.INCREASE, quantity, reason, at);
    }

    /**
     * Disminuye el stock físico, sin dejarlo por debajo de lo reservado.
     *
     * @param quantity cantidad a disminuir.
     * @param reason    motivo del movimiento (opcional).
     * @param at        momento del movimiento.
     * @throws BusinessRuleViolationException si no hay stock suficiente o se reduce por debajo de lo reservado.
     */
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

    /**
     * Ajusta el stock a un valor objetivo (conteo físico), sin dejarlo por debajo de lo reservado.
     *
     * @param targetQuantity valor objetivo de stock físico.
     * @param reason         motivo del ajuste (opcional).
     * @param at             momento del movimiento.
     * @throws BusinessRuleViolationException si el objetivo es negativo o queda por debajo de lo reservado.
     */
    public void adjust(int targetQuantity, String reason, Instant at) {
        requireNonNegative(targetQuantity, "targetQuantity");
        if (targetQuantity < reserved) {
            throw new BusinessRuleViolationException("El stock no puede quedar por debajo de lo reservado");
        }
        int delta = targetQuantity - onHand;
        onHand = targetQuantity;
        registerMovement(StockMovementType.ADJUST, delta, reason, at);
    }

    /**
     * Establece el umbral de stock mínimo.
     *
     * @param minimumStock nuevo umbral.
     * @throws BusinessRuleViolationException si el umbral es negativo.
     */
    public void setMinimumStock(int minimumStock) {
        requireNonNegative(minimumStock, "minimumStock");
        this.minimumStock = minimumStock;
    }

    /**
     * Reserva stock validando la disponibilidad (previene overselling).
     *
     * @param quantity  cantidad a reservar.
     * @param expiresAt momento de vencimiento de la reserva.
     * @param at        momento actual.
     * @return la reserva creada en estado {@code ACTIVE}.
     * @throws BusinessRuleViolationException si no hay stock suficiente o {@code expiresAt} no es posterior a {@code at}.
     * @throws NullPointerException           si {@code expiresAt} o {@code at} son {@code null}.
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

    /**
     * Consume una reserva activa: descuenta el stock y marca la reserva como confirmada.
     *
     * @param id identificador de la reserva.
     * @param at momento de la operación.
     * @throws ResourceNotFoundException si la reserva no existe.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
    public void confirmReservation(StockReservationId id, Instant at) {
        StockReservation reservation = requireActive(id, "confirmar");
        replace(reservation.withStatus(ReservationStatus.CONFIRMED));
        reserved -= reservation.quantity();
        onHand -= reservation.quantity();
        registerMovement(StockMovementType.CONFIRM, -reservation.quantity(), null, at);
    }

    /**
     * Libera una reserva activa: devuelve el stock reservado.
     *
     * @param id identificador de la reserva.
     * @param at momento de la operación.
     * @throws ResourceNotFoundException si la reserva no existe.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
    public void releaseReservation(StockReservationId id, Instant at) {
        StockReservation reservation = requireActive(id, "liberar");
        replace(reservation.withStatus(ReservationStatus.RELEASED));
        reserved -= reservation.quantity();
        registerMovement(StockMovementType.RELEASE, -reservation.quantity(), null, at);
    }

    /**
     * Expira una reserva activa: devuelve el stock reservado.
     *
     * @param id identificador de la reserva.
     * @param at momento de la operación.
     * @throws ResourceNotFoundException si la reserva no existe.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
    public void expireReservation(StockReservationId id, Instant at) {
        StockReservation reservation = requireActive(id, "expirar");
        replace(reservation.withStatus(ReservationStatus.EXPIRED));
        reserved -= reservation.quantity();
        registerMovement(StockMovementType.EXPIRE, -reservation.quantity(), null, at);
    }

    /**
     * Busca una reserva activa por identificador.
     *
     * @param id        identificador de la reserva.
     * @param operacion operación en curso (para el mensaje de error).
     * @return la reserva activa.
     * @throws ResourceNotFoundException   si la reserva no existe.
     * @throws BusinessRuleViolationException si la reserva no está activa.
     */
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

    /**
     * Reemplaza una reserva en la lista por su versión actualizada.
     *
     * @param updated reserva actualizada.
     */
    private void replace(StockReservation updated) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).id().equals(updated.id())) {
                reservations.set(i, updated);
                return;
            }
        }
    }

    /**
     * Registra un movimiento de inventario.
     *
     * @param type     tipo de movimiento.
     * @param quantity delta aplicado.
     * @param reason   motivo (opcional).
     * @param at       momento del movimiento.
     */
    private void registerMovement(StockMovementType type, int quantity, String reason, Instant at) {
        movements.add(new StockMovement(type, quantity, reason, at));
    }

    /**
     * Valida que el valor sea positivo.
     *
     * @param value valor a validar.
     * @param name  nombre del campo para el mensaje de error.
     * @throws BusinessRuleViolationException si {@code value} es menor o igual a cero.
     */
    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new BusinessRuleViolationException(name + " debe ser positivo");
        }
    }

    /**
     * Valida que el valor no sea negativo.
     *
     * @param value valor a validar.
     * @param name  nombre del campo para el mensaje de error.
     * @throws BusinessRuleViolationException si {@code value} es negativo.
     */
    private static void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new BusinessRuleViolationException(name + " no puede ser negativo");
        }
    }
}