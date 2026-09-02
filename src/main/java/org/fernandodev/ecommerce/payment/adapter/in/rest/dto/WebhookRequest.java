package org.fernandodev.ecommerce.payment.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Callback del proveedor (webhook).
 */
public record WebhookRequest(
        @NotBlank String transactionId,
        @NotBlank String status
) {
}