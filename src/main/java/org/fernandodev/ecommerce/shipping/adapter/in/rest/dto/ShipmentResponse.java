package org.fernandodev.ecommerce.shipping.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el detalle de un envío.
 */
public record ShipmentResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        UUID shippingMethodId,
        UUID addressId,
        String addressSnapshot,
        String trackingNumber,
        MoneyResponse cost,
        String status,
        List<StatusEntryResponse> statusHistory,
        Instant createdAt,
        Instant updatedAt
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    public record StatusEntryResponse(String status, Instant at) {
    }
}