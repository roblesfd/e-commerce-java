package org.fernandodev.ecommerce.checkout.adapter.out;

import org.fernandodev.ecommerce.checkout.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link PaymentPort} consultando el contexto Payments.
 * <p>
 * Inicia el pago en el contexto Payment con el requestId del checkout (la clave
 * de idempotencia), lo que previene pagos duplicados en reintentos.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PaymentContextPaymentPort implements PaymentPort {

    private final PaymentUseCases paymentUseCases;

    @Override
    public PaymentResult initiate(OrderId orderId, Money amount, String requestId) {
        Payment payment = paymentUseCases.initiate(orderId, amount, requestId);
        boolean approved = payment.getStatus() == PaymentStatus.AUTHORIZED;
        return new PaymentResult(payment.getId().value(), approved);
    }
}