package org.fernandodev.ecommerce.payment.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;

/**
 * Puerto de salida de persistencia del agregado {@link Payment}.
 */
public interface PaymentRepository {

    /**
     * Persiste el pago.
     */
    Payment save(Payment payment);

    /**
     * Consulta un pago por identificador.
     */
    Optional<Payment> findById(PaymentId id);

    /**
     * Busca el pago bloqueando la fila (PESSIMISTIC_WRITE) para transiciones
     * concurrentes (captura/refund/webhooks).
     */
    Optional<Payment> findByIdForUpdate(PaymentId id);

    /**
     * Consulta el pago de una orden.
     */
    Optional<Payment> findByOrderId(OrderId orderId);

    /**
     * Consulta un pago por la transacción del proveedor.
     */
    Optional<Payment> findByProviderTransactionId(String transactionId);

    /**
     * Indica si ya existe un pago con esa clave de idempotencia.
     */
    boolean existsByRequestId(String requestId);

    /**
     * Lista todos los pagos.
     */
    List<Payment> findAll();
}