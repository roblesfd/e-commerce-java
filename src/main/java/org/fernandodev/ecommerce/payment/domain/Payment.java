package org.fernandodev.ecommerce.payment.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentEvent;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Payments.
 * <p>
 * Modela el ciclo de vida del pago con transiciones explícitas e idempotentes:
 * un fallo de red no implica que el pago falló (spec §29); los estados
 * desconocidos del proveedor se registran sin asumir éxito ni fallo.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    private PaymentId id;
    private OrderId orderId;
    private String requestId;
    private Money amount;
    private PaymentStatus status;
    private String providerTransactionId;
    private Money refundedAmount;
    @Getter(AccessLevel.NONE)
    private List<PaymentEvent> events;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Crea un pago en estado {@code PENDING}.
     *
     * @param id        identificador del pago.
     * @param orderId   orden a pagar.
     * @param requestId clave de idempotencia.
     * @param amount    importe.
     * @param at        momento de creación.
     * @return el pago creado.
     * @throws BusinessRuleViolationException si el monto no es positivo.
     */
    public static Payment create(PaymentId id, OrderId orderId, String requestId, Money amount, Instant at) {
        Payment payment = new Payment();
        payment.id = Objects.requireNonNull(id, "id no puede ser null");
        payment.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        payment.requestId = Objects.requireNonNull(requestId, "requestId no puede ser null");
        payment.amount = Objects.requireNonNull(amount, "amount no puede ser null");
        if (!amount.isPositive()) {
            throw new BusinessRuleViolationException("El monto debe ser positivo");
        }
        payment.status = PaymentStatus.PENDING;
        payment.providerTransactionId = null;
        payment.refundedAmount = null;
        payment.events = new ArrayList<>();
        payment.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        payment.updatedAt = at;
        payment.addEvent("CREATED", null, at);
        return payment;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     *
     * @return el pago reconstruido.
     */
    public static Payment crear(
            PaymentId id,
            OrderId orderId,
            String requestId,
            Money amount,
            PaymentStatus status,
            String providerTransactionId,
            Money refundedAmount,
            List<PaymentEvent> events,
            Instant createdAt,
            Instant updatedAt
    ) {
        Payment payment = new Payment();
        payment.id = Objects.requireNonNull(id, "id no puede ser null");
        payment.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        payment.requestId = Objects.requireNonNull(requestId, "requestId no puede ser null");
        payment.amount = Objects.requireNonNull(amount, "amount no puede ser null");
        payment.status = Objects.requireNonNull(status, "status no puede ser null");
        payment.providerTransactionId = providerTransactionId;
        payment.refundedAmount = refundedAmount;
        payment.events = new ArrayList<>(events == null ? List.of() : events);
        payment.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        payment.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return payment;
    }

    /**
     * @return el historial de eventos del pago como lista inmutable.
     */
    public List<PaymentEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Autoriza el pago (PENDING → AUTHORIZED/FAILED).
     *
     * @return {@code true} si se autorizó.
     */
    /**
     * Autoriza el pago (PENDING → AUTHORIZED o FAILED).
     *
     * @param transactionId identificador de transacción del proveedor.
     * @param approved      si el proveedor aprobó la autorización.
     * @param at            momento de la operación.
     * @return {@code true} si se autorizó.
     * @throws BusinessRuleViolationException si el pago no está {@code PENDING}.
     */
    public boolean authorize(String transactionId, boolean approved, Instant at) {
        requireState(PaymentStatus.PENDING, "autorizar");
        this.providerTransactionId = transactionId;
        this.status = approved ? PaymentStatus.AUTHORIZED : PaymentStatus.FAILED;
        this.updatedAt = at;
        addEvent(approved ? "AUTHORIZED" : "FAILED", transactionId, at);
        return approved;
    }

    /**
     * Captura el pago (AUTHORIZED → CAPTURED). Idempotente: si ya está capturado
     * es un no-op (evita capturas duplicadas).
     *
     * @return {@code true} si la captura se aplicó.
     */
    /**
     * Captura el pago (AUTHORIZED → CAPTURED). Idempotente: si ya está capturado
     * es un no-op (evita capturas duplicadas).
     *
     * @param transactionId identificador de transacción del proveedor.
     * @param at            momento de la operación.
     * @return {@code true} si la captura se aplicó.
     * @throws BusinessRuleViolationException si el pago no está {@code AUTHORIZED}.
     */
    public boolean capture(String transactionId, Instant at) {
        if (status == PaymentStatus.CAPTURED) {
            return false;
        }
        requireState(PaymentStatus.AUTHORIZED, "capturar");
        this.providerTransactionId = transactionId;
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = at;
        addEvent("CAPTURED", transactionId, at);
        return true;
    }

    /**
     * Rechaza el pago (PENDING → FAILED).
     */
    /**
     * Rechaza el pago (PENDING → FAILED).
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si el pago no está {@code PENDING}.
     */
    public void reject(Instant at) {
        requireState(PaymentStatus.PENDING, "rechazar");
        this.status = PaymentStatus.FAILED;
        this.updatedAt = at;
        addEvent("FAILED", "rechazado por el proveedor", at);
    }

    /**
     * Cancela el pago (PENDING o AUTHORIZED → CANCELLED).
     */
    /**
     * Cancela el pago (PENDING o AUTHORIZED → CANCELLED).
     *
     * @param at momento de la operación.
     * @throws BusinessRuleViolationException si el pago no está {@code PENDING} o {@code AUTHORIZED}.
     */
    public void cancel(Instant at) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.AUTHORIZED) {
            throw new BusinessRuleViolationException("Solo se puede cancelar un pago PENDING o AUTHORIZED");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = at;
        addEvent("CANCELLED", null, at);
    }

    /**
     * Reembolsa el pago por un monto (parcial o completo). Idempotente: si ya
     * está reembolsado por completo es un no-op. Acumula {@code refundedAmount}
     * y pasa a REFUNDED solo cuando el acumulado cubre el monto total.
     *
     * @return {@code true} si el reembolso se aplicó.
     */
    /**
     * Reembolsa el pago por un monto (parcial o completo). Idempotente: si ya
     * está reembolsado por completo es un no-op. Acumula {@code refundedAmount}
     * y pasa a REFUNDED solo cuando el acumulado cubre el monto total.
     *
     * @param transactionId identificador de transacción del proveedor.
     * @param refundAmount  monto a reembolsar.
     * @param at            momento de la operación.
     * @return {@code true} si el reembolso se aplicó.
     * @throws BusinessRuleViolationException si excede el monto del pago o no está {@code CAPTURED}.
     */
    public boolean refund(String transactionId, Money refundAmount, Instant at) {
        if (status == PaymentStatus.REFUNDED) {
            return false;
        }
        requireState(PaymentStatus.CAPTURED, "reembolsar");
        Money current = refundedAmount == null ? Money.zero(amount.currencyCode()) : refundedAmount;
        Money totalRefunded = current.add(refundAmount);
        if (totalRefunded.isGreaterThan(amount)) {
            throw new BusinessRuleViolationException("El reembolso excede el monto del pago");
        }
        this.refundedAmount = totalRefunded;
        this.providerTransactionId = transactionId;
        this.updatedAt = at;
        if (totalRefunded.equals(amount)) {
            this.status = PaymentStatus.REFUNDED;
            addEvent("REFUNDED", transactionId, at);
        } else {
            addEvent("PARTIAL_REFUND", transactionId, at);
        }
        return true;
    }

    /**
     * Registra un estado desconocido del proveedor sin asumir éxito ni fallo.
     */
    /**
     * Registra un estado desconocido del proveedor sin asumir éxito ni fallo.
     *
     * @param detail detalle del estado desconocido.
     * @param at     momento de la operación.
     */
    public void recordUnknownState(String detail, Instant at) {
        addEvent("UNKNOWN_STATE", detail, at);
        this.updatedAt = at;
    }

    /**
     * @return {@code true} si el pago está en un estado terminal.
     */
    public boolean isTerminal() {
        return status == PaymentStatus.FAILED
                || status == PaymentStatus.CANCELLED
                || status == PaymentStatus.REFUNDED;
    }

    /**
     * Valida que el pago esté en el estado esperado.
     *
     * @param expected  estado esperado.
     * @param operacion operación en curso.
     * @throws BusinessRuleViolationException si el estado no coincide.
     */
    private void requireState(PaymentStatus expected, String operacion) {
        if (status != expected) {
            throw new BusinessRuleViolationException(
                    "No se puede " + operacion + " un pago en estado " + status);
        }
    }

    /**
     * Registra un evento en el historial del pago.
     *
     * @param type   tipo de evento.
     * @param detail detalle (opcional).
     * @param at     momento del evento.
     */
    private void addEvent(String type, String detail, Instant at) {
        events.add(new PaymentEvent(type, detail, at));
    }
}