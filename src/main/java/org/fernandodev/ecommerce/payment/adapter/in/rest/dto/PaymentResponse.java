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

    /**
     * Importe monetario en la respuesta.
     *
     * @param amount   cantidad.
     * @param currency divisa ISO 4217.
     */
    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    /**
     * Evento del historial en la respuesta.
     *
     * @param type   tipo de evento.
     * @param detail detalle (opcional).
     * @param at     momento del evento.
     */
    public record EventResponse(String type, String detail, Instant at) {
    }
}