package org.fernandodev.ecommerce.customer.adapter.config;

import org.fernandodev.ecommerce.customer.application.service.CustomerService;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.customer.application.port.out.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el servicio de aplicación (independiente de Spring) con su puerto de
 * persistencia.
 */
@Configuration
public class CustomerConfig {

    /**
     * @param customerRepository puerto de persistencia del cliente.
     * @return servicio de aplicación del contexto Customer.
     */
    @Bean
    public CustomerUseCases customerUseCases(CustomerRepository customerRepository) {
        return new CustomerService(customerRepository);
    }
}
