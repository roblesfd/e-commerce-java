package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.CartData;

/**
 * Proveedor del carrito del cliente (contexto Shopping Cart).
 */
public interface CartProvider {

/**
     * @param customerId identificador del cliente.
     * @return el carrito del cliente.
     */
    CartData getCart(UUID customerId);
}