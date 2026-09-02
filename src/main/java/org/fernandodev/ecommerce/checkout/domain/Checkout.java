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

    public List<CheckoutItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<ReservationReference> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void markOrderCreated(OrderId orderId, Instant at) {
        requireTransition(CheckoutStatus.ORDER_CREATED, "crear la orden");
        this.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        this.status = CheckoutStatus.ORDER_CREATED;
        this.updatedAt = at;
    }

    public void markPaymentInitiated(UUID paymentId, Instant at) {
        requireTransition(CheckoutStatus.PAYMENT_PENDING, "iniciar el pago");
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId no puede ser null");
        this.status = CheckoutStatus.PAYMENT_PENDING;
        this.updatedAt = at;
    }

    public void confirm(Instant at) {
        requireTransition(CheckoutStatus.CONFIRMED, "confirmar el checkout");
        this.status = CheckoutStatus.CONFIRMED;
        this.updatedAt = at;
    }

    public void fail(Instant at) {
        if (status != CheckoutStatus.PAYMENT_PENDING && status != CheckoutStatus.PENDING) {
            throw new BusinessRuleViolationException("No se puede marcar como fallido un checkout en estado " + status);
        }
        this.status = CheckoutStatus.FAILED;
        this.updatedAt = at;
    }

    public void cancel(Instant at) {
        if (status != CheckoutStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede cancelar un checkout PENDING");
        }
        this.status = CheckoutStatus.CANCELLED;
        this.updatedAt = at;
    }

    public boolean isTerminal() {
        return status == CheckoutStatus.CONFIRMED
                || status == CheckoutStatus.FAILED
                || status == CheckoutStatus.CANCELLED;
    }

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