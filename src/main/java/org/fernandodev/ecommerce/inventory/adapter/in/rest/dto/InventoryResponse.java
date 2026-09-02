package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el detalle de un inventario.
 */
public record InventoryResponse(
        UUID id,
        UUID productId,
        UUID variantId,
        int onHand,
        int reserved,
        int available,
        int minimumStock,
        boolean lowStock,
        List<StockMovementResponse> movements,
        List<StockReservationResponse> reservations
) {

    public record StockMovementResponse(String type, int quantity, String reason, Instant at) {
    }

    public record StockReservationResponse(
            UUID id,
            int quantity,
            String status,
            Instant createdAt,
            Instant expiresAt
    ) {
    }
}