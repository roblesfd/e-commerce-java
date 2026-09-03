package org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear o renombrar una wishlist.
 */
public record CreateWishlistRequest(
        @NotBlank @Size(max = 100) String name
) {
}