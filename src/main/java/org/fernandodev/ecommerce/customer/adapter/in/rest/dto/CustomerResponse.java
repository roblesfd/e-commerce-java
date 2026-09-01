package org.fernandodev.ecommerce.customer.adapter.in.rest.dto;

import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el perfil completo de un cliente.
 */
public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        List<AddressResponse> addresses,
        UUID defaultAddressId,
        PreferencesResponse preferences
) {
    public record PreferencesResponse(boolean newsletterEnabled, String preferredCurrencyCode) {
    }
}