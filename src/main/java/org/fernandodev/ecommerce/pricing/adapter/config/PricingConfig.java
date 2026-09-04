package org.fernandodev.ecommerce.pricing.adapter.config;

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
    /**
     * @param priceRepository puerto de persistencia de precios.
     * @return servicio de aplicación del contexto Pricing.
     */
    public PricingUseCases pricingUseCases(PriceRepository priceRepository) {
        return new PricingService(priceRepository);
    }
}
