package org.fernandodev.ecommerce.pricing.adapter;

import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.application.service.PricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el servicio de aplicación (independiente de Spring) del contexto
 * Pricing con su puerto de persistencia.
 */
@Configuration
public class PricingConfig {

    @Bean
    public PricingUseCases pricingUseCases(PriceRepository priceRepository) {
        return new PricingService(priceRepository);
    }
}