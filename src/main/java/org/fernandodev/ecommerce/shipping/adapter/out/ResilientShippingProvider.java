package org.fernandodev.ecommerce.shipping.adapter.out;

import java.util.function.Supplier;

import org.fernandodev.ecommerce.shipping.application.port.out.ShippingProviderPort;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Envuelve el proveedor de envíos con Resilience4j (Retry + CircuitBreaker)
 * para manejar timeouts y fallos del proveedor.
 */
public class ResilientShippingProvider implements ShippingProviderPort {

    private final ShippingProviderPort delegate;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public ResilientShippingProvider(ShippingProviderPort delegate, Retry retry, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public String createShipment(String carrier, String addressSnapshot) {
        Supplier<String> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker, Retry.decorateSupplier(retry,
                        () -> delegate.createShipment(carrier, addressSnapshot)));
        return decorated.get();
    }
}