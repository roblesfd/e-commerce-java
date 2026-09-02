package org.fernandodev.ecommerce.checkout.adapter.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Petición para iniciar un checkout.
 */
public record StartCheckoutRequest(
        @NotNull UUID addressId
) {
}