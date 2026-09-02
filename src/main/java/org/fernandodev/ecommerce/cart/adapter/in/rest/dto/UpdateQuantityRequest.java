package org.fernandodev.ecommerce.cart.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;

/**
 * Petición para actualizar la cantidad de un item del carrito.
 */
public record UpdateQuantityRequest(
        @Positive int quantity
) {
}