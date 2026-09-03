package org.fernandodev.ecommerce.returns.adapter.out.adapters;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.returns.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link PaymentPort} hacia el contexto Payments.
 */
@Component
@RequiredArgsConstructor
public class ReturnsPaymentPort implements PaymentPort {

    private final PaymentUseCases payments;

    @Override
    public boolean refund(OrderId orderId, Money amount) {
        Payment payment = payments.getByOrder(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para la orden: " + orderId));
        Payment refunded = payments.refund(payment.getId(), amount);
        return refunded.getStatus() == PaymentStatus.REFUNDED || refunded.getRefundedAmount() != null;
    }
}