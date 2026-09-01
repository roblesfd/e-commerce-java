package org.fernandodev.ecommerce.customer.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para actualizar las preferencias del cliente.
 */
public record PreferencesRequest(
        boolean newsletterEnabled,
        @NotBlank @Size(min = 3, max = 3) String preferredCurrencyCode
) {
}