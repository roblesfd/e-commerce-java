package org.fernandodev.ecommerce.order.adapter.config;

import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.application.port.out.OrderRepository;
import org.fernandodev.ecommerce.order.application.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el servicio de aplicación (independiente de Spring) del contexto
 * Order Management con su puerto de persistencia.
 */
@Configuration
public class OrderConfig {

    /**
     * @param orderRepository puerto de persistencia de órdenes.
     * @return servicio de aplicación del contexto Order Management.
     */
    @Bean
    public OrderUseCases orderUseCases(OrderRepository orderRepository) {
        return new OrderService(orderRepository);
    }
}
