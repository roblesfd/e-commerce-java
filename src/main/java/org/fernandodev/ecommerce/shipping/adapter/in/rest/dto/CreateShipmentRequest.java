package org.fernandodev.ecommerce.shipping.adapter.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Petición para crear un envío.
 */
public record CreateShipmentRequest(
        @NotNull UUID orderId,
        @NotNull UUID shippingMethodId,
        @NotNull UUID addressId
) {
}