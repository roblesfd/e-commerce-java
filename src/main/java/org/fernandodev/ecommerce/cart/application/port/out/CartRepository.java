package org.fernandodev.ecommerce.cart.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.domain.Cart;

/**
 * Puerto de salida de persistencia del agregado {@link Cart} (Redis).
 */
public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findByOwner(UUID ownerId);

    void delete(UUID ownerId);
}