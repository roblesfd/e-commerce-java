package org.fernandodev.ecommerce.inventory.application.dto;

/**
 * Datos para incrementar el stock.
 *
 * @param quantity cantidad a añadir.
 * @param reason   motivo del movimiento (opcional).
 */
public record IncreaseStockCommand(int quantity, String reason) {
}