package org.fernandodev.ecommerce.shipping.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Callback del proveedor de envíos (webhook).
 */
public record ShipmentWebhookRequest(
        @NotBlank String trackingNumber,
        @NotBlank String status
) {
}