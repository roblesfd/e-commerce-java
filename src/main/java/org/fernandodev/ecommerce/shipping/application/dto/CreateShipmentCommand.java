package org.fernandodev.ecommerce.shipping.application.dto;

import java.util.UUID;

import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;

/**
 * Datos para crear un envío para una orden confirmada.
 */
public record CreateShipmentCommand(
        ShippingMethodId shippingMethodId,
        UUID addressId
) {
}