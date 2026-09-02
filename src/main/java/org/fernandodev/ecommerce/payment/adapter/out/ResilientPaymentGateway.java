package org.fernandodev.ecommerce.payment.adapter.out;

import java.util.function.Supplier;

import org.fernandodev.ecommerce.payment.application.port.out.PaymentGateway;
import org.fernandodev.ecommerce.shared.domain.Money;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Envuelve la pasarela de pago con Resilience4j (Retry + CircuitBreaker) sobre
 * las llamadas al proveedor externo (spec §18).
 */
public class ResilientPaymentGateway implements PaymentGateway {

    private final PaymentGateway delegate;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public ResilientPaymentGateway(PaymentGateway delegate, Retry retry, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public GatewayResult authorize(String transactionId, Money amount) {
        return execute(() -> delegate.authorize(transactionId, amount));
    }

    @Override
    public GatewayResult capture(String transactionId, Money amount) {
        return execute(() -> delegate.capture(transactionId, amount));
    }

    @Override
    public GatewayResult refund(String transactionId, Money amount) {
        return execute(() -> delegate.refund(transactionId, amount));
    }

    @Override
    public GatewayResult cancel(String transactionId, Money amount) {
        return execute(() -> delegate.cancel(transactionId, amount));
    }

    private GatewayResult execute(Supplier<GatewayResult> supplier) {
        Supplier<GatewayResult> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker, Retry.decorateSupplier(retry, supplier));
        return decorated.get();
    }
}