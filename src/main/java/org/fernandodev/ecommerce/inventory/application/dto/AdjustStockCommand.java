package org.fernandodev.ecommerce.inventory.application.dto;

/**
 * Datos para ajustar el stock a un valor objetivo (conteo físico).
 *
 * @param targetQuantity valor objetivo de on_hand.
 * @param reason         motivo del ajuste (opcional).
 */
public record AdjustStockCommand(int targetQuantity, String reason) {
}