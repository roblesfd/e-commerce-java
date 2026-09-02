package org.fernandodev.ecommerce.inventory.application.dto;

import java.time.Instant;

/**
 * Datos para reservar stock.
 *
 * @param quantity  cantidad a reservar.
 * @param expiresAt momento de vencimiento de la reserva.
 */
public record ReserveStockCommand(int quantity, Instant expiresAt) {
}