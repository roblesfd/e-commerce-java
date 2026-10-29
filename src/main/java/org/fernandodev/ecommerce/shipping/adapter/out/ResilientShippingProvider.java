package org.fernandodev.ecommerce.shipping.adapter.out;

import java.util.function.Supplier;

import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentTrackingResult;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingProviderPort;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingQuoteRequest;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingQuoteResult;

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

    @Override
    public ShippingQuoteResult quote(ShippingQuoteRequest request) {
        Supplier<ShippingQuoteResult> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, () -> delegate.quote(request))
        );
        return decorated.get();
    }

    @Override
    public ShipmentTrackingResult trackShipment(String providerShipmentId) {
        Supplier<ShipmentTrackingResult> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, () -> delegate.trackShipment(providerShipmentId))
        );
        return decorated.get();
    }

    @Override
    public void cancelShipment(String providerShipmentId) {
        Runnable decorated = CircuitBreaker.decorateRunnable(
                circuitBreaker,
                Retry.decorateRunnable(retry, () -> delegate.cancelShipment(providerShipmentId))
        );
        decorated.run();
    }
}
