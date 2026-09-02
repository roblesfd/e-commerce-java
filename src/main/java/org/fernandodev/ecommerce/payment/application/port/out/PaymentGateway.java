package org.fernandodev.ecommerce.payment.application.port.out;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Puerta de pago (abstracción del proveedor externo).
 * <p>
 * El dominio depende de este puerto, no de un proveedor concreto (spec §29).
 * Los adapters de proveedores externos lo implementan.
 * </p>
 */
public interface PaymentGateway {

    GatewayResult authorize(String transactionId, Money amount);

    GatewayResult capture(String transactionId, Money amount);

    GatewayResult refund(String transactionId, Money amount);

    GatewayResult cancel(String transactionId, Money amount);

    enum GatewayOutcome {
        SUCCESS,
        FAILURE,
        UNKNOWN
    }

    record GatewayResult(String transactionId, GatewayOutcome outcome) {
    }
}