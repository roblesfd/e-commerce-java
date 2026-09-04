package org.fernandodev.ecommerce.payment.adapter;

import org.fernandodev.ecommerce.payment.adapter.out.ResilientPaymentGateway;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentGateway;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.application.service.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Cablea el servicio de aplicación (independiente de Spring) del contexto
 * Payments con su repositorio y la pasarela (envuelta en resiliencia).
 */
@Configuration
public class PaymentConfig {

    /**

     * Configura un bean del contexto Payments.

     */

    @Bean
    public PaymentUseCases paymentUseCases(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            Retry paymentGatewayRetry,
            CircuitBreaker paymentGatewayCircuitBreaker
    ) {
        ResilientPaymentGateway resilient = new ResilientPaymentGateway(
                paymentGateway, paymentGatewayRetry, paymentGatewayCircuitBreaker);
        return new PaymentService(paymentRepository, resilient);
    }
}