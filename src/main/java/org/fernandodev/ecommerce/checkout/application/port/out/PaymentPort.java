package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Puerta de pago: abstracción sobre el contexto Payments.
 */
public interface PaymentPort {

    PaymentResult initiate(OrderId orderId, Money amount, String requestId);

    record PaymentResult(UUID paymentId, boolean approved) {
    }
}