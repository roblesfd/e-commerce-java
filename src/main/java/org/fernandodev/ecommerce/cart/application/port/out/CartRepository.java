package org.fernandodev.ecommerce.cart.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.domain.Cart;

/**
 * Puerto de salida de persistencia del agregado {@link Cart} (Redis).
 */
public interface CartRepository {

    /**
     * Persiste el carrito.
     *
     * @param cart carrito a guardar.
     * @return el carrito persistido.
     */
    Cart save(Cart cart);

    /**
     * @param ownerId identificador del propietario (usuario IAM).
     * @return el carrito del propietario, o {@code Optional.empty()} si no existe.
     */
    Optional<Cart> findByOwner(UUID ownerId);

    /**
     * Elimina el carrito del propietario.
     *
     * @param ownerId identificador del propietario (usuario IAM).
     */
    void delete(UUID ownerId);
}