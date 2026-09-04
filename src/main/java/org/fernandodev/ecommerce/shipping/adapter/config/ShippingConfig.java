package org.fernandodev.ecommerce.shipping.adapter.config;

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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración del contexto {@code Shipping}.
 *
 * <p>Define los beans necesarios para integrar los servicios de aplicación
 * del contexto con sus puertos de dominio y aplicación, manteniendo la
 * configuración de Spring fuera de la lógica de negocio.</p>
 *
 * <p>También configura los mecanismos de resiliencia utilizados para
 * proteger las comunicaciones con el proveedor externo de envíos,
 * mediante {@link Retry} y {@link CircuitBreaker}.</p>
 */
@Configuration
public class ShippingConfig {

    /**
     * Configura la política de reintentos para las comunicaciones con
     * el proveedor externo de envíos.
     *
     * <p>La política permite un máximo de tres intentos y establece
     * una espera de 100 milisegundos entre ellos.</p>
     *
     * @return configuración de reintentos para el proveedor de envíos.
     */
    @Bean
    public Retry shippingProviderRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();

        return Retry.of("shippingProvider", config);
    }

    /**
     * Configura el circuit breaker utilizado para proteger las
     * comunicaciones con el proveedor externo de envíos.
     *
     * <p>El circuito considera una tasa de fallos del 50 % como umbral
     * para activar la protección y utiliza una ventana deslizante
     * de cinco llamadas para evaluar el comportamiento del proveedor.</p>
     *
     * @return circuit breaker configurado para el proveedor de envíos.
     */
    @Bean
    public CircuitBreaker shippingProviderCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .build();

        return CircuitBreaker.of("shippingProvider", config);
    }

    /**
     * Construye el servicio de aplicación responsable de gestionar
     * el ciclo de vida de los envíos.
     *
     * <p>El {@link ShippingProviderPort} es decorado mediante
     * {@code ResilientShippingProvider} para incorporar los mecanismos
     * de reintento y circuit breaker sin introducir dependencias de
     * resiliencia en la lógica principal del servicio.</p>
     *
     * @param shipmentRepository repositorio utilizado para persistir
     *                           y recuperar envíos.
     * @param methodRepository repositorio de métodos de envío.
     * @param shippingProvider puerto utilizado para comunicarse con
     *                          el proveedor externo de envíos.
     * @param orderPort puerto utilizado para interactuar con el contexto
     *                  Order.
     * @param addressProvider proveedor utilizado para obtener la dirección
     *                        del cliente asociada al envío.
     * @param shippingProviderRetry política de reintentos configurada
     *                              para el proveedor.
     * @param shippingProviderCircuitBreaker circuit breaker utilizado
     *                                       para proteger la comunicación
     *                                       con el proveedor.
     * @return implementación de {@link ShipmentUseCases} encargada
     *         de gestionar los casos de uso de envíos.
     */
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
                shippingProvider,
                shippingProviderRetry,
                shippingProviderCircuitBreaker
        );

        return new ShipmentService(
                shipmentRepository,
                methodRepository,
                resilient,
                orderPort,
                addressProvider
        );
    }

    /**
     * Construye el servicio de aplicación encargado de gestionar
     * los métodos de envío.
     *
     * @param methodRepository repositorio utilizado para persistir
     *                         y consultar los métodos de envío.
     * @return implementación de {@link ShippingMethodUseCases} encargada
     *         de gestionar los casos de uso relacionados con los métodos
     *         de envío.
     */
    @Bean
    public ShippingMethodUseCases shippingMethodUseCases(
            ShippingMethodRepository methodRepository
    ) {
        return new ShippingMethodService(methodRepository);
    }
}
