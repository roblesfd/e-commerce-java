package org.fernandodev.ecommerce.wishlist.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;

/**
 * Puerto de salida de persistencia del agregado {@link Wishlist}.
 */
public interface WishlistRepository {

    Wishlist save(Wishlist wishlist);

    Optional<Wishlist> findById(WishlistId id);

    List<Wishlist> findByCustomerId(UUID customerId);

    void delete(WishlistId id);
}