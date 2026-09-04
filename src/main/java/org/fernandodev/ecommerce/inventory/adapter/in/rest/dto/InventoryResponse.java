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

    /**
     * Movimiento de inventario en la respuesta.
     *
     * @param type     tipo de movimiento.
     * @param quantity delta aplicado.
     * @param reason   motivo (opcional).
     * @param at       momento del movimiento.
     */
    public record StockMovementResponse(String type, int quantity, String reason, Instant at) {
    }

    /**
     * Reserva de stock en la respuesta.
     *
     * @param id         identificador de la reserva.
     * @param quantity   cantidad reservada.
     * @param status     estado de la reserva.
     * @param createdAt  momento de creación.
     * @param expiresAt  momento de vencimiento.
     */
    public record StockReservationResponse(
            UUID id,
            int quantity,
            String status,
            Instant createdAt,
            Instant expiresAt
    ) {
    }
}