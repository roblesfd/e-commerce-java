package org.fernandodev.ecommerce.customer.adapter.in.rest.dto;

import java.util.UUID;

/**
 * Respuesta con una dirección del cliente.
 */
public record AddressResponse(
        UUID id,
        String street,
        String city,
        String state,
        String postalCode,
        String country
) {
}