package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Orquestación administrativa de pagos y reembolsos.
 */
public interface AdminPaymentUseCases {

    List<Payment> listAllPayments();

    Payment getPayment(PaymentId id);

    Payment refundPayment(PaymentId id, Money amount);
}