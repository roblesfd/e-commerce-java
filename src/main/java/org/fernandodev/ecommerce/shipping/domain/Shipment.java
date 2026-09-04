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
 *
 * <p>El agregado encapsula el estado del envío, la información necesaria
 * para su entrega, el costo asociado y el historial de transiciones de
 * estado.</p>
 *
 * <p>La integración con el contexto {@code Order} se realiza mediante
 * puertos de aplicación. Este agregado no modifica directamente el modelo
 * de persistencia de la orden; las actualizaciones correspondientes se
 * realizan mediante los casos de uso o puertos definidos para dicha
 * integración.</p>
 *
 * <p>El ciclo de vida del envío se controla mediante transiciones explícitas
 * que impiden avanzar hacia estados incompatibles con el estado actual.
 * Los estados {@link ShipmentStatus#DELIVERED},
 * {@link ShipmentStatus#FAILED} y {@link ShipmentStatus#CANCELLED}
 * representan estados terminales.</p>
 *
 * <p>El historial de estados se mantiene internamente como una colección
 * mutable, pero se expone mediante una vista no modificable para preservar
 * la encapsulación del agregado.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Shipment {

    /**
     * Identificador único del envío.
     */
    private ShipmentId id;

    /**
     * Identificador de la orden asociada al envío.
     */
    private OrderId orderId;

    /**
     * Identificador del cliente propietario de la orden.
     */
    private UUID customerId;

    /**
     * Identificador del método de envío seleccionado.
     */
    private ShippingMethodId shippingMethodId;

    /**
     * Identificador de la dirección utilizada para el envío.
     */
    private UUID addressId;

    /**
     * Instantánea de la dirección utilizada en el momento de crear
     * el envío.
     *
     * <p>Permite conservar los datos de la dirección utilizados para
     * el envío independientemente de modificaciones posteriores
     * realizadas sobre la dirección original.</p>
     */
    private String addressSnapshot;

    /**
     * Número de seguimiento asignado al envío.
     */
    private String trackingNumber;

    /**
     * Costo asociado al envío.
     */
    private Money cost;

    /**
     * Estado actual del envío.
     */
    private ShipmentStatus status;

    /**
     * Historial de estados por los que ha atravesado el envío.
     *
     * <p>El acceso generado por Lombok se deshabilita para este campo
     * porque la colección se expone mediante {@link #getStatusHistory()}
     * como una vista no modificable.</p>
     */
    @Getter(AccessLevel.NONE)
    private List<ShipmentStatusEntry> statusHistory;

    /**
     * Instante en el que se creó el envío.
     */
    private Instant createdAt;

    /**
     * Instante de la última actualización del envío.
     */
    private Instant updatedAt;

    /**
     * Crea un nuevo envío en estado {@link ShipmentStatus#PENDING}.
     *
     * <p>El historial del envío se inicializa automáticamente registrando
     * el estado {@code PENDING} junto con el instante de creación.</p>
     *
     * @param id identificador único del envío.
     * @param orderId identificador de la orden asociada.
     * @param customerId identificador del cliente.
     * @param shippingMethodId identificador del método de envío.
     * @param addressId identificador de la dirección utilizada.
     * @param addressSnapshot instantánea de la dirección utilizada
     *                       para el envío.
     * @param trackingNumber número de seguimiento del envío.
     * @param cost costo asociado al envío.
     * @param at instante de creación del envío.
     * @return nuevo {@code Shipment} en estado {@link ShipmentStatus#PENDING}.
     * @throws NullPointerException si {@code id}, {@code orderId},
     *         {@code customerId}, {@code shippingMethodId},
     *         {@code trackingNumber}, {@code cost} o {@code at} son
     *         {@code null}.
     */
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
        shipment.shippingMethodId = Objects.requireNonNull(
                shippingMethodId,
                "shippingMethodId no puede ser null"
        );
        shipment.addressId = addressId;
        shipment.addressSnapshot = addressSnapshot;
        shipment.trackingNumber = Objects.requireNonNull(
                trackingNumber,
                "trackingNumber no puede ser null"
        );
        shipment.cost = Objects.requireNonNull(cost, "cost no puede ser null");
        shipment.status = ShipmentStatus.PENDING;
        shipment.statusHistory = new ArrayList<>(
                List.of(new ShipmentStatusEntry(ShipmentStatus.PENDING, at))
        );
        shipment.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        shipment.updatedAt = at;
        return shipment;
    }

    /**
     * Reconstruye un envío con su estado completo.
     *
     * <p>Esta factoría está destinada principalmente a la reconstrucción
     * del agregado desde la persistencia, permitiendo restaurar el estado
     * actual y el historial de estados previamente registrado.</p>
     *
     * <p>El historial recibido se copia defensivamente para evitar que
     * modificaciones posteriores sobre la colección original alteren
     * directamente el estado interno del agregado.</p>
     *
     * @param id identificador único del envío.
     * @param orderId identificador de la orden asociada.
     * @param customerId identificador del cliente.
     * @param shippingMethodId identificador del método de envío.
     * @param addressId identificador de la dirección utilizada.
     * @param addressSnapshot instantánea de la dirección utilizada.
     * @param trackingNumber número de seguimiento del envío.
     * @param cost costo asociado al envío.
     * @param status estado actual del envío.
     * @param statusHistory historial de estados del envío.
     * @param createdAt instante de creación del envío.
     * @param updatedAt instante de la última actualización.
     * @return envío reconstruido con el estado proporcionado.
     * @throws NullPointerException si {@code id}, {@code orderId},
     *         {@code customerId}, {@code shippingMethodId}, {@code cost},
     *         {@code status}, {@code createdAt} o {@code updatedAt}
     *         son {@code null}.
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
        shipment.shippingMethodId = Objects.requireNonNull(
                shippingMethodId,
                "shippingMethodId no puede ser null"
        );
        shipment.addressId = addressId;
        shipment.addressSnapshot = addressSnapshot;
        shipment.trackingNumber = trackingNumber;
        shipment.cost = Objects.requireNonNull(cost, "cost no puede ser null");
        shipment.status = Objects.requireNonNull(status, "status no puede ser null");
        shipment.statusHistory = new ArrayList<>(
                statusHistory == null ? List.of() : statusHistory
        );
        shipment.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt no puede ser null"
        );
        shipment.updatedAt = Objects.requireNonNull(
                updatedAt,
                "updatedAt no puede ser null"
        );
        return shipment;
    }

    /**
     * Obtiene el historial de estados del envío.
     *
     * <p>Se devuelve una vista no modificable de la colección interna
     * para impedir que consumidores externos puedan alterar directamente
     * el historial del agregado.</p>
     *
     * @return lista no modificable con el historial de estados.
     */
    public List<ShipmentStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    /**
     * Marca el envío como preparado para continuar con el proceso
     * de despacho.
     *
     * <p>La transición válida es
     * {@link ShipmentStatus#PENDING} →
     * {@link ShipmentStatus#PREPARING}.</p>
     *
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición no es válida.
     */
    public void markPrepared(Instant at) {
        transition(ShipmentStatus.PREPARING, at);
    }

    /**
     * Marca el envío como despachado.
     *
     * <p>La transición válida es
     * {@link ShipmentStatus#PREPARING} →
     * {@link ShipmentStatus#SHIPPED}.</p>
     *
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición no es válida.
     */
    public void markShipped(Instant at) {
        transition(ShipmentStatus.SHIPPED, at);
    }

    /**
     * Marca el envío como actualmente en tránsito.
     *
     * <p>La transición válida es
     * {@link ShipmentStatus#SHIPPED} →
     * {@link ShipmentStatus#IN_TRANSIT}.</p>
     *
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición no es válida.
     */
    public void markInTransit(Instant at) {
        transition(ShipmentStatus.IN_TRANSIT, at);
    }

    /**
     * Marca el envío como fuera para entrega.
     *
     * <p>La transición válida es
     * {@link ShipmentStatus#IN_TRANSIT} →
     * {@link ShipmentStatus#OUT_FOR_DELIVERY}.</p>
     *
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición no es válida.
     */
    public void markOutForDelivery(Instant at) {
        transition(ShipmentStatus.OUT_FOR_DELIVERY, at);
    }

    /**
     * Marca el envío como entregado.
     *
     * <p>La transición válida es
     * {@link ShipmentStatus#OUT_FOR_DELIVERY} →
     * {@link ShipmentStatus#DELIVERED}. Una vez alcanzado este estado,
     * el envío se considera terminal.</p>
     *
     * @param at instante en el que se confirma la entrega.
     * @throws BusinessRuleViolationException si la transición no es válida.
     */
    public void markDelivered(Instant at) {
        transition(ShipmentStatus.DELIVERED, at);
    }

    /**
     * Marca el envío como fallido.
     *
     * <p>Un envío entregado o cancelado no puede volver a marcarse
     * como fallido. Para cualquier otro estado, la transición se
     * delega en la validación general del ciclo de vida.</p>
     *
     * @param at instante en el que se registra el fallo.
     * @throws BusinessRuleViolationException si el envío ya se encuentra
     *         en estado {@link ShipmentStatus#DELIVERED} o
     *         {@link ShipmentStatus#CANCELLED}, o si la transición
     *         no es válida.
     */
    public void handleFailure(Instant at) {
        if (status == ShipmentStatus.DELIVERED
                || status == ShipmentStatus.CANCELLED) {

            throw new BusinessRuleViolationException(
                    "No se puede marcar como fallido un envío " + status
            );
        }

        transition(ShipmentStatus.FAILED, at);
    }

    /**
     * Cancela el envío.
     *
     * <p>La cancelación únicamente está permitida cuando el envío se
     * encuentra en estado {@link ShipmentStatus#PENDING} o
     * {@link ShipmentStatus#PREPARING}.</p>
     *
     * @param at instante en el que se realiza la cancelación.
     * @throws BusinessRuleViolationException si el envío no se encuentra
     *         en un estado que permita su cancelación.
     */
    public void cancel(Instant at) {
        if (status != ShipmentStatus.PENDING
                && status != ShipmentStatus.PREPARING) {

            throw new BusinessRuleViolationException(
                    "Solo se puede cancelar un envío PENDING o PREPARING"
            );
        }

        transition(ShipmentStatus.CANCELLED, at);
    }

    /**
     * Registra la detección de un estado desconocido durante la integración
     * o procesamiento del envío.
     *
     * <p>Esta operación únicamente actualiza el instante de modificación
     * del agregado y no altera el estado actual ni agrega una entrada
     * al historial.</p>
     *
     * @param detail detalle asociado al estado desconocido.
     * @param at instante en el que se registra la situación.
     */
    public void recordUnknownState(String detail, Instant at) {
        this.updatedAt = at;
    }

    /**
     * Determina si el envío ha alcanzado un estado terminal.
     *
     * <p>Los estados terminales son
     * {@link ShipmentStatus#DELIVERED},
     * {@link ShipmentStatus#FAILED} y
     * {@link ShipmentStatus#CANCELLED}.</p>
     *
     * @return {@code true} si el envío se encuentra en un estado terminal;
     *         {@code false} en caso contrario.
     */
    public boolean isTerminal() {
        return status == ShipmentStatus.DELIVERED
                || status == ShipmentStatus.FAILED
                || status == ShipmentStatus.CANCELLED;
    }

    /**
     * Realiza una transición de estado del envío.
     *
     * <p>Antes de modificar el estado, se valida que la transición sea
     * permitida mediante {@link #canTransitionTo(ShipmentStatus)}.
     * Cuando la transición es válida, se actualizan el estado y la fecha
     * de modificación y se agrega una nueva entrada al historial.</p>
     *
     * @param target nuevo estado al que se desea transicionar.
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición desde
     *         el estado actual hacia {@code target} no está permitida.
     */
    private void transition(ShipmentStatus target, Instant at) {
        if (!canTransitionTo(target)) {
            throw new BusinessRuleViolationException(
                    "Transición inválida de envío: " + status + " → " + target
            );
        }

        this.status = target;
        this.updatedAt = at;
        statusHistory.add(new ShipmentStatusEntry(target, at));
    }

    /**
     * Determina si el envío puede realizar una transición hacia
     * el estado indicado.
     *
     * <p>Las reglas definen explícitamente las transiciones permitidas
     * para cada estado objetivo:</p>
     *
     * <ul>
     *     <li>{@code PENDING → PREPARING}</li>
     *     <li>{@code PREPARING → SHIPPED}</li>
     *     <li>{@code SHIPPED → IN_TRANSIT}</li>
     *     <li>{@code IN_TRANSIT → OUT_FOR_DELIVERY}</li>
     *     <li>{@code OUT_FOR_DELIVERY → DELIVERED}</li>
     *     <li>Cualquier estado no terminal → {@code FAILED}</li>
     *     <li>{@code PENDING/PREPARING → CANCELLED}</li>
     * </ul>
     *
     * @param target estado al que se desea realizar la transición.
     * @return {@code true} si la transición está permitida;
     *         {@code false} en caso contrario.
     */
    private boolean canTransitionTo(ShipmentStatus target) {
        return switch (target) {
            case PREPARING -> status == ShipmentStatus.PENDING;
            case SHIPPED -> status == ShipmentStatus.PREPARING;
            case IN_TRANSIT -> status == ShipmentStatus.SHIPPED;
            case OUT_FOR_DELIVERY -> status == ShipmentStatus.IN_TRANSIT;
            case DELIVERED -> status == ShipmentStatus.OUT_FOR_DELIVERY;
            case FAILED ->
                    status != ShipmentStatus.DELIVERED
                            && status != ShipmentStatus.CANCELLED;
            case CANCELLED ->
                    status == ShipmentStatus.PENDING
                            || status == ShipmentStatus.PREPARING;
            default -> false;
        };
    }
}
