package org.fernandodev.ecommerce.checkout.application.dto;

import java.util.UUID;

/**
 * Datos para iniciar un checkout.
 *
 * @param addressId dirección seleccionada del cliente.
 */
public record StartCheckoutCommand(UUID addressId) {
}