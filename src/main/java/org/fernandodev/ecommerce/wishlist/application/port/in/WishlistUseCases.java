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

    /**
     * Crea una wishlist para el cliente.
     */
    Wishlist create(UUID customerId, String name);

    /**
     * Consulta una wishlist por identificador.
     */
    Wishlist getById(WishlistId id);

    /**
     * Lista las wishlists de un cliente.
     */
    List<Wishlist> listByCustomer(UUID customerId);

    /**
     * Renombra una wishlist.
     */
    Wishlist rename(WishlistId id, String name);

    /**
     * Elimina una wishlist.
     */
    void delete(WishlistId id);

    /**
     * Añade un item validando producto activo y stock.
     */
    Wishlist addItem(WishlistId id, AddItemCommand command);

    /**
     * Elimina un item de la wishlist.
     */
    Wishlist removeItem(WishlistId id, ProductId productId, ProductVariantId variantId);

    /**
     * Vacía la wishlist.
     */
    Wishlist clear(WishlistId id);
}