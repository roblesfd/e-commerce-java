package org.fernandodev.ecommerce.returns.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.domain.value_object.RefundStatus;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;

/**
 * Respuesta de una solicitud de devolución.
 */
public record ReturnResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        ReturnReason reason,
        ReturnStatus status,
        String trackingNumber,
        BigDecimal refundAmount,
        String refundCurrency,
        RefundStatus refundStatus,
        boolean refundRequested,
        InspectionResponse inspection,
        List<ItemResponse> items,
        List<StatusEntryResponse> statusHistory,
        Instant createdAt,
        Instant updatedAt
) {

    public record InspectionResponse(String condition, String notes, boolean passed, Instant at) {
    }

    public record ItemResponse(UUID orderItemId, UUID productId, int quantity,
                               BigDecimal refundAmount, String currency) {
    }

    public record StatusEntryResponse(ReturnStatus status, Instant at) {
    }
}