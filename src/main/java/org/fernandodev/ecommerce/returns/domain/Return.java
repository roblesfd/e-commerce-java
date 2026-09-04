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
 * Agregado raíz que representa una solicitud de devolución de una orden.
 *
 * <p>El agregado gestiona el ciclo de vida de una devolución, desde su
 * solicitud inicial hasta su finalización, rechazo o cancelación.</p>
 *
 * <p>También mantiene la información necesaria para gestionar la inspección
 * de los artículos devueltos y realizar el seguimiento del reembolso
 * asociado.</p>
 *
 * <p>El reembolso se solicita al contexto Payment al completar el retorno
 * ({@code INSPECTED -> COMPLETED}) y su estado se mantiene en el agregado
 * para evitar solicitudes de reembolso duplicadas.</p>
 *
 * <p>El agregado garantiza que las transiciones de estado se realicen
 * únicamente cuando corresponda según el ciclo de vida definido para
 * una devolución.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Return {

    /**
     * Identificador único de la devolución.
     */
    private ReturnId id;

    /**
     * Identificador de la orden asociada a la devolución.
     */
    private OrderId orderId;

    /**
     * Identificador del cliente que solicita la devolución.
     */
    private UUID customerId;

    /**
     * Motivo por el cual se solicita la devolución.
     */
    private ReturnReason reason;

    /**
     * Artículos incluidos en la devolución.
     *
     * <p>La colección se expone mediante {@link #getItems()} como una
     * colección no modificable para proteger el estado interno del agregado.</p>
     */
    @Getter(AccessLevel.NONE)
    private List<ReturnItem> items;

    /**
     * Estado actual de la devolución dentro de su ciclo de vida.
     */
    private ReturnStatus status;

    /**
     * Número de seguimiento asociado al envío de devolución.
     */
    private String trackingNumber;

    /**
     * Importe total que debe ser reembolsado al cliente.
     */
    private Money refundAmount;

    /**
     * Estado actual del proceso de reembolso.
     */
    private RefundStatus refundStatus;

    /**
     * Indica si ya se ha solicitado el reembolso al contexto Payment.
     *
     * <p>Este indicador permite distinguir entre una devolución que todavía
     * no ha generado una solicitud de reembolso y una devolución cuyo
     * reembolso ya fue solicitado.</p>
     */
    private boolean refundRequested;

    /**
     * Resultado de la inspección realizada sobre los artículos devueltos.
     */
    private InspectionResult inspection;

    /**
     * Historial de las transiciones de estado de la devolución.
     *
     * <p>La colección se expone mediante {@link #getStatusHistory()} como
     * una colección no modificable.</p>
     */
    @Getter(AccessLevel.NONE)
    private List<ReturnStatusEntry> statusHistory;

    /**
     * Fecha y hora en la que se creó la devolución.
     */
    private Instant createdAt;

    /**
     * Fecha y hora de la última modificación de la devolución.
     */
    private Instant updatedAt;

    /**
     * Crea una nueva devolución en estado {@link ReturnStatus#REQUESTED}.
     *
     * <p>La devolución se crea sin información de seguimiento, inspección
     * ni reembolso. El historial de estados se inicializa registrando
     * la creación de la devolución.</p>
     *
     * @param id identificador único de la devolución.
     * @param orderId identificador de la orden asociada.
     * @param customerId identificador del cliente que solicita la devolución.
     * @param reason motivo de la devolución.
     * @param items artículos incluidos en la devolución.
     * @param at instante en el que se crea la devolución.
     * @return una nueva instancia de {@code Return} en estado
     *         {@link ReturnStatus#REQUESTED}.
     * @throws NullPointerException si alguno de los parámetros obligatorios
     *         es {@code null}.
     * @throws BusinessRuleViolationException si la devolución no contiene
     *         al menos un artículo.
     */
    public static Return create(
            ReturnId id,
            OrderId orderId,
            UUID customerId,
            ReturnReason reason,
            List<ReturnItem> items,
            Instant at
    ) {
        Return aReturn = new Return();

        aReturn.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );
        aReturn.orderId = Objects.requireNonNull(
                orderId,
                "orderId no puede ser null"
        );
        aReturn.customerId = Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );
        aReturn.reason = Objects.requireNonNull(
                reason,
                "reason no puede ser null"
        );

        aReturn.items = new ArrayList<>(
                items == null ? List.of() : items
        );

        if (aReturn.items.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "La devolución requiere al menos un item"
            );
        }

        aReturn.status = ReturnStatus.REQUESTED;
        aReturn.trackingNumber = null;
        aReturn.refundAmount = null;
        aReturn.refundStatus = null;
        aReturn.refundRequested = false;
        aReturn.inspection = null;

        aReturn.statusHistory = new ArrayList<>(
                List.of(new ReturnStatusEntry(
                        ReturnStatus.REQUESTED,
                        at
                ))
        );

        aReturn.createdAt = Objects.requireNonNull(
                at,
                "at no puede ser null"
        );
        aReturn.updatedAt = at;

        return aReturn;
    }

    /**
     * Reconstruye una devolución existente a partir de su estado completo.
     *
     * <p>Este método permite reconstruir el agregado desde la información
     * almacenada por la infraestructura de persistencia. Las colecciones
     * recibidas se copian defensivamente para evitar compartir referencias
     * mutables con el exterior.</p>
     *
     * @param id identificador único de la devolución.
     * @param orderId identificador de la orden asociada.
     * @param customerId identificador del cliente.
     * @param reason motivo de la devolución.
     * @param items artículos incluidos en la devolución.
     * @param status estado actual de la devolución.
     * @param trackingNumber número de seguimiento del envío de devolución.
     * @param refundAmount importe del reembolso.
     * @param refundStatus estado actual del reembolso.
     * @param refundRequested indica si ya se solicitó el reembolso.
     * @param inspection resultado de la inspección de los artículos.
     * @param statusHistory historial de estados de la devolución.
     * @param createdAt fecha y hora de creación.
     * @param updatedAt fecha y hora de la última actualización.
     * @return una instancia de {@code Return} reconstruida con el estado
     *         proporcionado.
     * @throws NullPointerException si alguno de los campos obligatorios
     *         es {@code null}.
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

        aReturn.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );
        aReturn.orderId = Objects.requireNonNull(
                orderId,
                "orderId no puede ser null"
        );
        aReturn.customerId = Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );
        aReturn.reason = Objects.requireNonNull(
                reason,
                "reason no puede ser null"
        );

        aReturn.items = new ArrayList<>(
                items == null ? List.of() : items
        );

        aReturn.status = Objects.requireNonNull(
                status,
                "status no puede ser null"
        );
        aReturn.trackingNumber = trackingNumber;
        aReturn.refundAmount = refundAmount;
        aReturn.refundStatus = refundStatus;
        aReturn.refundRequested = refundRequested;
        aReturn.inspection = inspection;

        aReturn.statusHistory = new ArrayList<>(
                statusHistory == null ? List.of() : statusHistory
        );

        aReturn.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt no puede ser null"
        );
        aReturn.updatedAt = Objects.requireNonNull(
                updatedAt,
                "updatedAt no puede ser null"
        );

        return aReturn;
    }

    /**
     * Obtiene los artículos incluidos en la devolución.
     *
     * <p>La colección devuelta no puede modificarse directamente, protegiendo
     * así el estado interno del agregado.</p>
     *
     * @return lista no modificable de artículos de la devolución.
     */
    public List<ReturnItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Obtiene el historial de estados de la devolución.
     *
     * <p>La colección devuelta no puede modificarse directamente, garantizando
     * que las modificaciones del historial se realicen únicamente mediante
     * las transiciones controladas por el agregado.</p>
     *
     * @return lista no modificable con el historial de estados.
     */
    public List<ReturnStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    /**
     * Obtiene el importe del reembolso.
     *
     * <p>Si todavía no se ha determinado un importe de reembolso,
     * el resultado será {@code null}.</p>
     *
     * @return importe del reembolso o {@code null} si todavía no ha sido
     *         establecido.
     */
    public Money refundAmountOrZero() {
        return refundAmount == null ? null : refundAmount;
    }

    /**
     * Aprueba la solicitud de devolución y establece el importe a reembolsar.
     *
     * <p>La devolución solo puede aprobarse cuando se encuentra en estado
     * {@link ReturnStatus#REQUESTED}.</p>
     *
     * @param refundAmount importe que deberá ser reembolsado al cliente.
     * @param at instante en el que se aprueba la devolución.
     * @throws NullPointerException si {@code refundAmount} o {@code at}
     *         son {@code null}.
     * @throws BusinessRuleViolationException si la devolución no se encuentra
     *         en un estado que permita su aprobación.
     */
    public void approve(Money refundAmount, Instant at) {
        requireTransition(
                ReturnStatus.APPROVED,
                "aprobar",
                at
        );

        this.refundAmount = Objects.requireNonNull(
                refundAmount,
                "refundAmount no puede ser null"
        );
        this.updatedAt = at;
    }

    /**
     * Rechaza la solicitud de devolución.
     *
     * <p>La devolución solo puede rechazarse cuando se encuentra en estado
     * {@link ReturnStatus#REQUESTED}.</p>
     *
     * @param at instante en el que se rechaza la devolución.
     * @throws BusinessRuleViolationException si la transición no está
     *         permitida para el estado actual.
     */
    public void reject(Instant at) {
        requireTransition(
                ReturnStatus.REJECTED,
                "rechazar",
                at
        );
    }

    /**
     * Cancela la devolución.
     *
     * <p>Una devolución únicamente puede cancelarse mientras se encuentra
     * en estado {@link ReturnStatus#REQUESTED} o
     * {@link ReturnStatus#APPROVED}.</p>
     *
     * @param at instante en el que se cancela la devolución.
     * @throws BusinessRuleViolationException si el estado actual no permite
     *         cancelar la devolución.
     */
    public void cancel(Instant at) {
        if (status != ReturnStatus.REQUESTED
                && status != ReturnStatus.APPROVED) {
            throw new BusinessRuleViolationException(
                    "Solo se puede cancelar una devolución REQUESTED o APPROVED"
            );
        }

        transition(
                ReturnStatus.CANCELLED,
                at
        );
    }

    /**
     * Registra el envío de los artículos devueltos.
     *
     * <p>La devolución debe encontrarse en estado
     * {@link ReturnStatus#APPROVED} para pasar a
     * {@link ReturnStatus#IN_TRANSIT}.</p>
     *
     * @param trackingNumber número de seguimiento del envío.
     * @param at instante en el que se registra el envío.
     * @throws NullPointerException si {@code trackingNumber} es {@code null}.
     * @throws BusinessRuleViolationException si la transición no está
     *         permitida para el estado actual.
     */
    public void registerReturnShipment(
            String trackingNumber,
            Instant at
    ) {
        requireTransition(
                ReturnStatus.IN_TRANSIT,
                "registrar el envío de devolución",
                at
        );

        this.trackingNumber = Objects.requireNonNull(
                trackingNumber,
                "trackingNumber no puede ser null"
        );
        this.updatedAt = at;
    }

    /**
     * Registra la recepción física de los artículos devueltos.
     *
     * <p>La devolución debe encontrarse en estado
     * {@link ReturnStatus#IN_TRANSIT} para pasar a
     * {@link ReturnStatus#RECEIVED}.</p>
     *
     * @param at instante en el que se reciben los artículos.
     */
    public void receive(Instant at) {
        requireTransition(
                ReturnStatus.RECEIVED,
                "recibir",
                at
        );
    }

    /**
     * Registra el resultado de la inspección de los artículos devueltos.
     *
     * <p>La devolución debe encontrarse en estado
     * {@link ReturnStatus#RECEIVED} para pasar a
     * {@link ReturnStatus#INSPECTED}.</p>
     *
     * @param inspection resultado de la inspección realizada.
     * @param at instante en el que se realiza la inspección.
     * @throws NullPointerException si {@code inspection} es {@code null}.
     * @throws BusinessRuleViolationException si la transición no está
     *         permitida para el estado actual.
     */
    public void inspect(
            InspectionResult inspection,
            Instant at
    ) {
        requireTransition(
                ReturnStatus.INSPECTED,
                "inspeccionar",
                at
        );

        this.inspection = Objects.requireNonNull(
                inspection,
                "inspection no puede ser null"
        );
        this.updatedAt = at;
    }

    /**
     * Completa la devolución.
     *
     * <p>La devolución únicamente puede completarse después de haber sido
     * inspeccionada, realizando la transición
     * {@link ReturnStatus#INSPECTED} -> {@link ReturnStatus#COMPLETED}.</p>
     *
     * @param at instante en el que se completa la devolución.
     * @throws BusinessRuleViolationException si la devolución no se encuentra
     *         en estado {@link ReturnStatus#INSPECTED}.
     */
    public void complete(Instant at) {
        requireTransition(
                ReturnStatus.COMPLETED,
                "completar",
                at
        );
    }

    /**
     * Marca el reembolso como solicitado.
     *
     * <p>Al solicitar el reembolso, su estado pasa a
     * {@link RefundStatus#PENDING}.</p>
     *
     * @param at instante en el que se solicita el reembolso.
     */
    public void markRefundRequested(Instant at) {
        this.refundRequested = true;
        this.refundStatus = RefundStatus.PENDING;
        this.updatedAt = at;
    }

    /**
     * Marca el reembolso como completado correctamente.
     *
     * <p>El estado del reembolso pasa a
     * {@link RefundStatus#REFUNDED}.</p>
     *
     * @param at instante en el que se confirma el reembolso.
     */
    public void markRefundSucceeded(Instant at) {
        this.refundStatus = RefundStatus.REFUNDED;
        this.updatedAt = at;
    }

    /**
     * Marca el reembolso como fallido.
     *
     * <p>El estado del reembolso pasa a
     * {@link RefundStatus#FAILED}, permitiendo posteriormente determinar
     * si puede realizarse un nuevo intento.</p>
     *
     * @param at instante en el que se registra el fallo del reembolso.
     */
    public void markRefundFailed(Instant at) {
        this.refundStatus = RefundStatus.FAILED;
        this.updatedAt = at;
    }

    /**
     * Determina si el reembolso puede volver a intentarse.
     *
     * <p>Un nuevo intento únicamente está permitido cuando el reembolso
     * ya fue solicitado, falló y la devolución se encuentra completada.</p>
     *
     * @return {@code true} si el reembolso puede volver a intentarse;
     *         {@code false} en caso contrario.
     */
    public boolean canRetryRefund() {
        return refundRequested
                && refundStatus == RefundStatus.FAILED
                && status == ReturnStatus.COMPLETED;
    }

    /**
     * Determina si la devolución se encuentra en un estado terminal.
     *
     * <p>Los estados terminales son {@link ReturnStatus#COMPLETED},
     * {@link ReturnStatus#REJECTED} y {@link ReturnStatus#CANCELLED}.</p>
     *
     * @return {@code true} si la devolución se encuentra en un estado
     *         terminal; {@code false} en caso contrario.
     */
    public boolean isTerminal() {
        return status == ReturnStatus.COMPLETED
                || status == ReturnStatus.REJECTED
                || status == ReturnStatus.CANCELLED;
    }

    /**
     * Valida y ejecuta una transición de estado de la devolución.
     *
     * <p>Define las transiciones permitidas dentro del ciclo de vida
     * de la devolución:</p>
     *
     * <ul>
     *     <li>{@code REQUESTED -> APPROVED}</li>
     *     <li>{@code REQUESTED -> REJECTED}</li>
     *     <li>{@code APPROVED -> IN_TRANSIT}</li>
     *     <li>{@code IN_TRANSIT -> RECEIVED}</li>
     *     <li>{@code RECEIVED -> INSPECTED}</li>
     *     <li>{@code INSPECTED -> COMPLETED}</li>
     * </ul>
     *
     * @param target estado al que se desea realizar la transición.
     * @param operacion descripción de la operación que origina la transición.
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición no está
     *         permitida desde el estado actual.
     */
    private void requireTransition(
            ReturnStatus target,
            String operacion,
            Instant at
    ) {
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
                    "No se puede "
                            + operacion
                            + " una devolución en estado "
                            + status
            );
        }

        transition(
                target,
                at
        );
    }

    /**
     * Ejecuta una transición de estado y registra el cambio en el historial.
     *
     * <p>Además de actualizar el estado actual, actualiza la fecha de
     * modificación y agrega una nueva entrada al historial de estados.</p>
     *
     * @param target nuevo estado de la devolución.
     * @param at instante en el que se realiza la transición.
     */
    private void transition(
            ReturnStatus target,
            Instant at
    ) {
        this.status = target;
        this.updatedAt = at;
        statusHistory.add(
                new ReturnStatusEntry(
                        target,
                        at
                )
        );
    }
}
