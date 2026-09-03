package org.fernandodev.ecommerce.wishlist.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.wishlist.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Casos de uso de wishlists (puerto de entrada).
 */
public interface WishlistUseCases {

    Wishlist create(UUID customerId, String name);

    Wishlist getById(WishlistId id);

    List<Wishlist> listByCustomer(UUID customerId);

    Wishlist rename(WishlistId id, String name);

    void delete(WishlistId id);

    Wishlist addItem(WishlistId id, AddItemCommand command);

    Wishlist removeItem(WishlistId id, ProductId productId, ProductVariantId variantId);

    Wishlist clear(WishlistId id);
}