package org.fernandodev.ecommerce.cart.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición para aplicar un cupón al carrito.
 */
public record ApplyCouponRequest(
        @NotBlank String code
) {
}