package org.fernandodev.ecommerce.shipping.adapter;

import java.time.Duration;

import org.fernandodev.ecommerce.shipping.adapter.out.ResilientShippingProvider;
import org.fernandodev.ecommerce.shipping.application.port.in.ShipmentUseCases;
import org.fernandodev.ecommerce.shipping.application.port.in.ShippingMethodUseCases;
import org.fernandodev.ecommerce.shipping.application.port.out.CustomerAddressProvider;
import org.fernandodev.ecommerce.shipping.application.port.out.OrderPort;
import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentRepository;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingProviderPort;
import org.fernandodev.ecommerce.shipping.application.service.ShipmentService;
import org.fernandodev.ecommerce.shipping.application.service.ShippingMethodService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Cablea los servicios de aplicación (independientes de Spring) del contexto
 * Shipping con sus puertos y la resiliencia del proveedor.
 */
@Configuration
public class ShippingConfig {

    @Bean
    public Retry shippingProviderRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();
        return Retry.of("shippingProvider", config);
    }

    @Bean
    public CircuitBreaker shippingProviderCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .build();
        return CircuitBreaker.of("shippingProvider", config);
    }

    @Bean
    public ShipmentUseCases shipmentUseCases(
            ShipmentRepository shipmentRepository,
            ShippingMethodRepository methodRepository,
            ShippingProviderPort shippingProvider,
            OrderPort orderPort,
            CustomerAddressProvider addressProvider,
            Retry shippingProviderRetry,
            CircuitBreaker shippingProviderCircuitBreaker
    ) {
        ResilientShippingProvider resilient = new ResilientShippingProvider(
                shippingProvider, shippingProviderRetry, shippingProviderCircuitBreaker);
        return new ShipmentService(shipmentRepository, methodRepository, resilient, orderPort, addressProvider);
    }

    @Bean
    public ShippingMethodUseCases shippingMethodUseCases(ShippingMethodRepository methodRepository) {
        return new ShippingMethodService(methodRepository);
    }
}