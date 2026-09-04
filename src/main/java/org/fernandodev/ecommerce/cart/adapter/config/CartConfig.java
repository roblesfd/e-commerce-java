package org.fernandodev.ecommerce.cart.adapter.config;

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

    /**
     * @param cartRepository puerto de persistencia del carrito (Redis).
     * @param prices         proveedor de precios hacia Pricing.
     * @param availability   proveedor de disponibilidad hacia Inventory.
     * @param coupons        proveedor de cupones hacia Promotions.
     * @return servicio de aplicación del contexto Shopping Cart.
     */
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
