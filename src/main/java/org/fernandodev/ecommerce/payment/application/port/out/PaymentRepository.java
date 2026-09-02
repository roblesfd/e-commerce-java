package org.fernandodev.ecommerce.payment.application.port.out;

import java.util.Optional;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;

/**
 * Puerto de salida de persistencia del agregado {@link Payment}.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    /**
     * Busca el pago bloqueando la fila (PESSIMISTIC_WRITE) para transiciones
     * concurrentes (captura/refund/webhooks).
     */
    Optional<Payment> findByIdForUpdate(PaymentId id);

    Optional<Payment> findByOrderId(OrderId orderId);

    Optional<Payment> findByProviderTransactionId(String transactionId);

    boolean existsByRequestId(String requestId);
}