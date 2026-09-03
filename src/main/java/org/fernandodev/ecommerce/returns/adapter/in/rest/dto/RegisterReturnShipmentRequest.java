package org.fernandodev.ecommerce.returns.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para registrar el envío de devolución.
 */
public record RegisterReturnShipmentRequest(
        @NotBlank @Size(max = 128) String trackingNumber
) {
}