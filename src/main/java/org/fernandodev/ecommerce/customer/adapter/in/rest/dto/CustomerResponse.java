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
    /**
     * Preferencias del cliente en la respuesta.
     *
     * @param newsletterEnabled     si desea recibir novedades.
     * @param preferredCurrencyCode divisa preferida (ISO 4217).
     */
    public record PreferencesResponse(boolean newsletterEnabled, String preferredCurrencyCode) {
    }
}