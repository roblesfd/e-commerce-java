package org.fernandodev.ecommerce.admin.application.service;

import java.util.List;

import org.fernandodev.ecommerce.admin.application.port.in.AdminPaymentUseCases;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de pagos y reembolsos.
 */
@RequiredArgsConstructor
public class AdminPaymentService implements AdminPaymentUseCases {

    private final PaymentUseCases payments;
    private final AuditService audit;

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Payment> listAllPayments() {
        return payments.listAll();
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Payment getPayment(PaymentId id) {
        return payments.getById(id);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Payment refundPayment(PaymentId id, Money amount) {
        audit.record("REFUND_PAYMENT", "payment", id.value(),
                "Reembolso de " + amount.amount() + " " + amount.currencyCode());
        return payments.refund(id, amount);
    }
}