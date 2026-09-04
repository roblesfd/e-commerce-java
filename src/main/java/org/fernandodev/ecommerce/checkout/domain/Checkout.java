package org.fernandodev.ecommerce.checkout.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutStatus;
import org.fernandodev.ecommerce.checkout.domain.value_object.ReservationReference;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Checkout.
 * <p>
 * Orquesta la conversión del carrito en una orden con pago. Se persiste en Redis
 * (dato transitorio) y su {@code idempotencyKey} garantiza que un retry resuelva
 * a la misma operación lógica (spec §27).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Checkout {

    private CheckoutId id;
    private UUID customerId;
    private String idempotencyKey;
    private UUID cartId;
    private UUID addressId;
    private String addressSnapshot;
    @Getter(AccessLevel.NONE)
    private List<CheckoutItem> items;
    @Getter(AccessLevel.NONE)
    private List<ReservationReference> reservations;
    private String couponCode;
    private Money couponDiscount;
    private Money subtotal;
    private Money discounts;
    private Money total;
    private CheckoutStatus status;
    private OrderId orderId;
    private UUID paymentId;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Inicia un checkout a partir del carrito validado.
     *
     * @param id               identificador del checkout.
     * @param customerId       identificador del cliente.
     * @param idempotencyKey   clave de idempotencia del retry.
     * @param cartId           identificador del carrito.
     * @param items            items con precio vigente.
     * @param reservations     reservas de inventario realizadas.
     * @param couponCode       cupón aplicado (opcional).
     * @param couponDiscount   descuento del cupón (opcional).
     * @param subtotal         subtotal del carrito.
     * @param addressId        identificador de la dirección de envío.
     * @param addressSnapshot  copia de la dirección de envío.
     * @param at               momento de inicio.
     * @return el checkout en estado {@code PENDING}.
     */
    public static Checkout start(
            CheckoutId id,
            UUID customerId,
            String idempotencyKey,
            UUID cartId,
            List<CheckoutItem> items,
            List<ReservationReference> reservations,
            String couponCode,
            Money couponDiscount,
            Money subtotal,
            UUID addressId,
            String addressSnapshot,
            Instant at
    ) {
        return crear(id, customerId, idempotencyKey, cartId, addressId, addressSnapshot, items,
                reservations, couponCode, couponDiscount, subtotal,
                couponDiscount == null ? Money.zero(subtotal.currencyCode()) : couponDiscount,
                subtotal.subtract(couponDiscount == null ? Money.zero(subtotal.currencyCode()) : couponDiscount),
                CheckoutStatus.PENDING, null, null, at, at);
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     *
     * @param id              identificador del checkout.
     * @param customerId      identificador del cliente.
     * @param idempotencyKey  clave de idempotencia.
     * @param cartId          identificador del carrito.
     * @param addressId       identificador de la dirección de envío.
     * @param addressSnapshot copia de la dirección de envío.
     * @param items           items del checkout.
     * @param reservations    reservas de inventario.
     * @param couponCode      cupón aplicado (opcional).
     * @param couponDiscount  descuento del cupón (opcional).
     * @param subtotal        subtotal.
     * @param discounts       descuentos aplicados.
     * @param total           total.
     * @param status          estado del checkout.
     * @param orderId         orden creada (null si aún no).
     * @param paymentId       pago iniciado (null si aún no).
     * @param createdAt       momento de creación.
     * @param updatedAt       momento de última modificación.
     * @return el checkout reconstruido.
     * @throws BusinessRuleViolationException si el checkout no tiene items.
     * @throws NullPointerException           si algún campo obligatorio es {@code null}.
     */
    public static Checkout crear(
            CheckoutId id,
            UUID customerId,
            String idempotencyKey,
            UUID cartId,
            UUID addressId,
            String addressSnapshot,
            List<CheckoutItem> items,
            List<ReservationReference> reservations,
            String couponCode,
            Money couponDiscount,
            Money subtotal,
            Money discounts,
            Money total,
            CheckoutStatus status,
            OrderId orderId,
            UUID paymentId,
            Instant createdAt,
            Instant updatedAt
    ) {
        Checkout checkout = new Checkout();
        checkout.id = Objects.requireNonNull(id, "id no puede ser null");
        checkout.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        checkout.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey no puede ser null");
        checkout.cartId = Objects.requireNonNull(cartId, "cartId no puede ser null");
        checkout.addressId = addressId;
        checkout.addressSnapshot = addressSnapshot;
        checkout.items = new ArrayList<>(items == null ? List.of() : items);
        if (checkout.items.isEmpty()) {
            throw new BusinessRuleViolationException("El checkout requiere al menos un item");
        }
        checkout.reservations = new ArrayList<>(reservations == null ? List.of() : reservations);
        checkout.couponCode = couponCode;
        checkout.couponDiscount = couponDiscount;
        checkout.subtotal = Objects.requireNonNull(subtotal, "subtotal no puede ser null");
        checkout.discounts = Objects.requireNonNull(discounts, "discounts no puede ser null");
        checkout.total = Objects.requireNonNull(total, "total no puede ser null");
        checkout.status = Objects.requireNonNull(status, "status no puede ser null");
        checkout.orderId = orderId;
        checkout.paymentId = paymentId;
        checkout.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        checkout.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return checkout;
    }

    /**
     * @return los items del checkout como lista inmutable.
     */
    public List<CheckoutItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @return las reservas de inventario como lista inmutable.
     */
    public List<ReservationReference> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    /**
     * Registra la creación de la orden asociada.
     *
     * @param orderId identificador de la orden creada.
     * @param at      momento de la operación.
     * @throws BusinessRuleViolationException si el checkout no está {@code PENDING}.
     * @throws NullPointerException           si {@code orderId} es {@code null}.
     */
    public void markOrderCreated(OrderId orderId, Instant at) {
        requireTransition(CheckoutStatus.ORDER_CREATED, "crear la orden");
        this.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        this.status = CheckoutStatus.ORDER_CREATED;
        this.updatedAt = at;
    }

    /**
     * Registra el inicio del pago.
     *
     * @param paymentId identificador del pago iniciado.
     * @param at        momento de la operación.
     * @throws BusinessRuleViolationException si el checkout no está {@code ORDER_CREATED}.
     * @throws NullPointerException           si {@code paymentId} es {@code null}.
     */
    public void markPaymentInitiated(UUID paymentId, Instant at) {
        requireTransition(CheckoutStatus.PAYMENT_PENDING, "iniciar el pago");
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId no puede ser null");
        this.status = CheckoutStatus.PAYMENT_PENDING;
        this.updatedAt = at;
    }

    /**
     * Confirma el checkout (pago aprobado y orden confirmada).
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si el checkout no está {@code PAYMENT_PENDING}.
     */
    public void confirm(Instant at) {
        requireTransition(CheckoutStatus.CONFIRMED, "confirmar el checkout");
        this.status = CheckoutStatus.CONFIRMED;
        this.updatedAt = at;
    }

    /**
     * Marca el checkout como fallido (pago rechazado).
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si el checkout no está {@code PENDING} o {@code PAYMENT_PENDING}.
     */
    public void fail(Instant at) {
        if (status != CheckoutStatus.PAYMENT_PENDING && status != CheckoutStatus.PENDING) {
            throw new BusinessRuleViolationException("No se puede marcar como fallido un checkout en estado " + status);
        }
        this.status = CheckoutStatus.FAILED;
        this.updatedAt = at;
    }

    /**
     * Cancela el checkout (solo en {@code PENDING}).
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si el checkout no está {@code PENDING}.
     */
    public void cancel(Instant at) {
        if (status != CheckoutStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede cancelar un checkout PENDING");
        }
        this.status = CheckoutStatus.CANCELLED;
        this.updatedAt = at;
    }

    /**
     * @return {@code true} si el checkout está en un estado terminal.
     */
    public boolean isTerminal() {
        return status == CheckoutStatus.CONFIRMED
                || status == CheckoutStatus.FAILED
                || status == CheckoutStatus.CANCELLED;
    }

    /**
     * Valida la transición permitida al estado objetivo.
     *
     * @param target    estado destino.
     * @param operacion operación en curso (para el mensaje de error).
     * @throws BusinessRuleViolationException si la transición no está permitida.
     */
    private void requireTransition(CheckoutStatus target, String operacion) {
        boolean valid = switch (target) {
            case ORDER_CREATED -> status == CheckoutStatus.PENDING;
            case PAYMENT_PENDING -> status == CheckoutStatus.ORDER_CREATED;
            case CONFIRMED -> status == CheckoutStatus.PAYMENT_PENDING;
            default -> false;
        };
        if (!valid) {
            throw new BusinessRuleViolationException(
                    "No se puede " + operacion + " un checkout en estado " + status);
        }
    }
}