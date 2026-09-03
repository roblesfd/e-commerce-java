package org.fernandodev.ecommerce.returns.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.domain.value_object.InspectionResult;
import org.fernandodev.ecommerce.returns.domain.value_object.RefundStatus;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnItem;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatusEntry;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una solicitud de devolución.
 * <p>
 * El reembolso se solicita al contexto Payment al completar el retorno
 * (INSPECTED → COMPLETED) y su estado se trackea en el agregado para evitar
 * reembolsos duplicados.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Return {

    private ReturnId id;
    private OrderId orderId;
    private UUID customerId;
    private ReturnReason reason;
    @Getter(AccessLevel.NONE)
    private List<ReturnItem> items;
    private ReturnStatus status;
    private String trackingNumber;
    private Money refundAmount;
    private RefundStatus refundStatus;
    private boolean refundRequested;
    private InspectionResult inspection;
    @Getter(AccessLevel.NONE)
    private List<ReturnStatusEntry> statusHistory;
    private Instant createdAt;
    private Instant updatedAt;

    public static Return create(ReturnId id, OrderId orderId, UUID customerId, ReturnReason reason,
                                List<ReturnItem> items, Instant at) {
        Return aReturn = new Return();
        aReturn.id = Objects.requireNonNull(id, "id no puede ser null");
        aReturn.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        aReturn.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        aReturn.reason = Objects.requireNonNull(reason, "reason no puede ser null");
        aReturn.items = new ArrayList<>(items == null ? List.of() : items);
        if (aReturn.items.isEmpty()) {
            throw new BusinessRuleViolationException("La devolución requiere al menos un item");
        }
        aReturn.status = ReturnStatus.REQUESTED;
        aReturn.trackingNumber = null;
        aReturn.refundAmount = null;
        aReturn.refundStatus = null;
        aReturn.refundRequested = false;
        aReturn.inspection = null;
        aReturn.statusHistory = new ArrayList<>(List.of(new ReturnStatusEntry(ReturnStatus.REQUESTED, at)));
        aReturn.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        aReturn.updatedAt = at;
        return aReturn;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Return crear(
            ReturnId id,
            OrderId orderId,
            UUID customerId,
            ReturnReason reason,
            List<ReturnItem> items,
            ReturnStatus status,
            String trackingNumber,
            Money refundAmount,
            RefundStatus refundStatus,
            boolean refundRequested,
            InspectionResult inspection,
            List<ReturnStatusEntry> statusHistory,
            Instant createdAt,
            Instant updatedAt
    ) {
        Return aReturn = new Return();
        aReturn.id = Objects.requireNonNull(id, "id no puede ser null");
        aReturn.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        aReturn.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        aReturn.reason = Objects.requireNonNull(reason, "reason no puede ser null");
        aReturn.items = new ArrayList<>(items == null ? List.of() : items);
        aReturn.status = Objects.requireNonNull(status, "status no puede ser null");
        aReturn.trackingNumber = trackingNumber;
        aReturn.refundAmount = refundAmount;
        aReturn.refundStatus = refundStatus;
        aReturn.refundRequested = refundRequested;
        aReturn.inspection = inspection;
        aReturn.statusHistory = new ArrayList<>(statusHistory == null ? List.of() : statusHistory);
        aReturn.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        aReturn.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return aReturn;
    }

    public List<ReturnItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<ReturnStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public Money refundAmountOrZero() {
        return refundAmount == null ? null : refundAmount;
    }

    public void approve(Money refundAmount, Instant at) {
        requireTransition(ReturnStatus.APPROVED, "aprobar", at);
        this.refundAmount = Objects.requireNonNull(refundAmount, "refundAmount no puede ser null");
        this.updatedAt = at;
    }

    public void reject(Instant at) {
        requireTransition(ReturnStatus.REJECTED, "rechazar", at);
    }

    public void cancel(Instant at) {
        if (status != ReturnStatus.REQUESTED && status != ReturnStatus.APPROVED) {
            throw new BusinessRuleViolationException("Solo se puede cancelar una devolución REQUESTED o APPROVED");
        }
        transition(ReturnStatus.CANCELLED, at);
    }

    public void registerReturnShipment(String trackingNumber, Instant at) {
        requireTransition(ReturnStatus.IN_TRANSIT, "registrar el envío de devolución", at);
        this.trackingNumber = Objects.requireNonNull(trackingNumber, "trackingNumber no puede ser null");
        this.updatedAt = at;
    }

    public void receive(Instant at) {
        requireTransition(ReturnStatus.RECEIVED, "recibir", at);
    }

    public void inspect(InspectionResult inspection, Instant at) {
        requireTransition(ReturnStatus.INSPECTED, "inspeccionar", at);
        this.inspection = Objects.requireNonNull(inspection, "inspection no puede ser null");
        this.updatedAt = at;
    }

    public void complete(Instant at) {
        requireTransition(ReturnStatus.COMPLETED, "completar", at);
    }

    public void markRefundRequested(Instant at) {
        this.refundRequested = true;
        this.refundStatus = RefundStatus.PENDING;
        this.updatedAt = at;
    }

    public void markRefundSucceeded(Instant at) {
        this.refundStatus = RefundStatus.REFUNDED;
        this.updatedAt = at;
    }

    public void markRefundFailed(Instant at) {
        this.refundStatus = RefundStatus.FAILED;
        this.updatedAt = at;
    }

    public boolean canRetryRefund() {
        return refundRequested && refundStatus == RefundStatus.FAILED && status == ReturnStatus.COMPLETED;
    }

    public boolean isTerminal() {
        return status == ReturnStatus.COMPLETED
                || status == ReturnStatus.REJECTED
                || status == ReturnStatus.CANCELLED;
    }

    private void requireTransition(ReturnStatus target, String operacion, Instant at) {
        boolean valid = switch (target) {
            case APPROVED -> status == ReturnStatus.REQUESTED;
            case REJECTED -> status == ReturnStatus.REQUESTED;
            case IN_TRANSIT -> status == ReturnStatus.APPROVED;
            case RECEIVED -> status == ReturnStatus.IN_TRANSIT;
            case INSPECTED -> status == ReturnStatus.RECEIVED;
            case COMPLETED -> status == ReturnStatus.INSPECTED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessRuleViolationException(
                    "No se puede " + operacion + " una devolución en estado " + status);
        }
        transition(target, at);
    }

    private void transition(ReturnStatus target, Instant at) {
        this.status = target;
        this.updatedAt = at;
        statusHistory.add(new ReturnStatusEntry(target, at));
    }
}