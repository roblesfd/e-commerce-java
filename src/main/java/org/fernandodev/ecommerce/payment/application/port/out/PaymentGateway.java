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

    /**
     * Autoriza un cargo por el monto.
     *
     * @param transactionId identificador de transacción.
     * @param amount        monto.
     * @return el resultado de la operación.
     */
    GatewayResult authorize(String transactionId, Money amount);

    /**
     * Captura un cargo autorizado.
     *
     * @param transactionId identificador de transacción.
     * @param amount        monto.
     * @return el resultado de la operación.
     */
    GatewayResult capture(String transactionId, Money amount);

    /**
     * Reembolsa un cargo.
     *
     * @param transactionId identificador de transacción.
     * @param amount        monto.
     * @return el resultado de la operación.
     */
    GatewayResult refund(String transactionId, Money amount);

    /**
     * Cancela un cargo.
     *
     * @param transactionId identificador de transacción.
     * @param amount        monto.
     * @return el resultado de la operación.
     */
    GatewayResult cancel(String transactionId, Money amount);

    /**
     * Resultado de la operación del proveedor.
     */
    enum GatewayOutcome {
        SUCCESS,
        FAILURE,
        UNKNOWN
    }

    /**
     * Resultado de una operación de la pasarela.
     *
     * @param transactionId identificador de transacción.
     * @param outcome       resultado.
     */
    record GatewayResult(String transactionId, GatewayOutcome outcome) {
    }
}