package org.fernandodev.ecommerce.checkout.adapter;

import org.fernandodev.ecommerce.checkout.application.port.in.CheckoutUseCases;
import org.fernandodev.ecommerce.checkout.application.port.out.CartProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.CheckoutRepository;
import org.fernandodev.ecommerce.checkout.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.CustomerProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.InventoryReservationPort;
import org.fernandodev.ecommerce.checkout.application.port.out.OrderPort;
import org.fernandodev.ecommerce.checkout.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.checkout.application.port.out.PriceProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.ProductProvider;
import org.fernandodev.ecommerce.checkout.application.service.CheckoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el servicio de aplicación (independiente de Spring) del contexto
 * Checkout con sus puertos hacia los demás contextos.
 */
@Configuration
public class CheckoutConfig {

    @Bean
    public CheckoutUseCases checkoutUseCases(
            CheckoutRepository checkoutRepository,
            CartProvider carts,
            ProductProvider products,
            PriceProvider prices,
            InventoryReservationPort inventory,
            CustomerProvider customers,
            CouponProvider coupons,
            OrderPort orders,
            PaymentPort payments
    ) {
        return new CheckoutService(checkoutRepository, carts, products, prices, inventory,
                customers, coupons, orders, payments);
    }
}