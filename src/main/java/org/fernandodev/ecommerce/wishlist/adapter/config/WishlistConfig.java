package org.fernandodev.ecommerce.wishlist.adapter.config;

import org.fernandodev.ecommerce.wishlist.application.port.in.WishlistUseCases;
import org.fernandodev.ecommerce.wishlist.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.wishlist.application.port.out.InventoryPort;
import org.fernandodev.ecommerce.wishlist.application.port.out.WishlistRepository;
import org.fernandodev.ecommerce.wishlist.application.service.WishlistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los servicios de aplicación del contexto Wishlist.
 */
@Configuration
public class WishlistConfig {

    @Bean
    public WishlistUseCases wishlistUseCases(WishlistRepository wishlists, CatalogPort catalog,
                                             InventoryPort inventory) {
        return new WishlistService(wishlists, catalog, inventory);
    }
}
