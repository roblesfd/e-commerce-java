package org.fernandodev.ecommerce.inventory.application.dto;

/**
 * Datos para disminuir el stock.
 *
 * @param quantity cantidad a restar.
 * @param reason   motivo del movimiento (opcional).
 */
public record DecreaseStockCommand(int quantity, String reason) {
}