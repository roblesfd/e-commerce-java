package org.fernandodev.ecommerce.payment.application.port.in;

import java.util.Optional;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Casos de uso del contexto Payments (puerto de entrada).
 */
public interface PaymentUseCases {

    /**
     * Crea el pago en PENDING y lo autoriza contra la pasarela. Idempotente por
     * {@code requestId}: un retry devuelve el pago existente.
     */
    Payment initiate(OrderId orderId, Money amount, String requestId);

    Payment authorize(PaymentId id);

    Payment capture(PaymentId id);

    Payment reject(PaymentId id);

    Payment cancel(PaymentId id);

    Payment refund(PaymentId id);

    Payment getById(PaymentId id);

    Optional<Payment> getByOrder(OrderId orderId);

    /**
     * Procesa un callback del proveedor (webhook). Idempotente: callbacks
     * duplicados o tardíos no duplican capturas/reembolsos.
     */
    Payment processWebhook(String transactionId, String providerStatus);
}