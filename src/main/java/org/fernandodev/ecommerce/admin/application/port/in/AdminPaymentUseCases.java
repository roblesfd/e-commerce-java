package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Orquestación administrativa de pagos y reembolsos.
 */
public interface AdminPaymentUseCases {

/**
     * Lista todos los pagos.
     */
    List<Payment> listAllPayments();

/**
     * Consulta un pago y su estado.
     */
    Payment getPayment(PaymentId id);

/**
     * Reembolsa un pago por un monto y lo registra en auditoría.
     */
    Payment refundPayment(PaymentId id, Money amount);
}