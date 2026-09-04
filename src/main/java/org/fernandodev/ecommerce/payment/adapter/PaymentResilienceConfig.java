package org.fernandodev.ecommerce.payment.adapter;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Configura los registros de resiliencia (Retry y CircuitBreaker) usados por la
 * pasarela de pago.
 */
@Configuration
public class PaymentResilienceConfig {

    /**

     * Configura un bean de resiliencia de la pasarela de pagos.

     */

    @Bean
    public Retry paymentGatewayRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();
        return Retry.of("paymentGateway", config);
    }

    /**

     * Configura un bean de resiliencia de la pasarela de pagos.

     */

    @Bean
    public CircuitBreaker paymentGatewayCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .build();
        return CircuitBreaker.of("paymentGateway", config);
    }
}