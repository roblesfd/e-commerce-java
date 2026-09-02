package org.fernandodev.ecommerce.payment.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el detalle de un pago.
 */
public record PaymentResponse(
        UUID id,
        UUID orderId,
        String requestId,
        MoneyResponse amount,
        String status,
        String providerTransactionId,
        List<EventResponse> events,
        Instant createdAt,
        Instant updatedAt
) {

    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    public record EventResponse(String type, String detail, Instant at) {
    }
}