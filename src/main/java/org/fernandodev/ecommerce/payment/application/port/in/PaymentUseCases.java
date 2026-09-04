package org.fernandodev.ecommerce.payment.application.port.in;

import java.util.List;
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

    /**
     * Autoriza un pago pendiente contra la pasarela.
     */
    Payment authorize(PaymentId id);

    /**
     * Captura un pago autorizado (idempotente).
     */
    Payment capture(PaymentId id);

    /**
     * Rechaza un pago pendiente.
     */
    Payment reject(PaymentId id);

    /**
     * Cancela un pago pendiente o autorizado.
     */
    Payment cancel(PaymentId id);

    /**
     * Reembolsa un pago capturado por un monto (parcial o completo).
     */
    Payment refund(PaymentId id, Money amount);

    /**
     * Consulta un pago por identificador.
     */
    Payment getById(PaymentId id);

    /**
     * Consulta el pago de una orden.
     */
    Optional<Payment> getByOrder(OrderId orderId);

    /**
     * Lista todos los pagos (uso administrativo).
     */
    List<Payment> listAll();

    /**
     * Procesa un callback del proveedor (webhook). Idempotente: callbacks
     * duplicados o tardíos no duplican capturas/reembolsos.
     */
    Payment processWebhook(String transactionId, String providerStatus);
}