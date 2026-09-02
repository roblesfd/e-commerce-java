package org.fernandodev.ecommerce.cart.adapter;

import org.fernandodev.ecommerce.cart.application.port.in.CartUseCases;
import org.fernandodev.ecommerce.cart.application.port.out.AvailabilityProvider;
import org.fernandodev.ecommerce.cart.application.port.out.CartRepository;
import org.fernandodev.ecommerce.cart.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.cart.application.port.out.PriceProvider;
import org.fernandodev.ecommerce.cart.application.service.CartService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el servicio de aplicación (independiente de Spring) del contexto
 * Shopping Cart con su repositorio y los proveedores hacia Pricing/Inventory/Promotions.
 */
@Configuration
public class CartConfig {

    @Bean
    public CartUseCases cartUseCases(
            CartRepository cartRepository,
            PriceProvider prices,
            AvailabilityProvider availability,
            CouponProvider coupons
    ) {
        return new CartService(cartRepository, prices, availability, coupons);
    }
}