package org.fernandodev.ecommerce.returns.application.port.out;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Puerto hacia el contexto Payments: solicitar reembolsos.
 */
public interface PaymentPort {

    boolean refund(OrderId orderId, Money amount);
}