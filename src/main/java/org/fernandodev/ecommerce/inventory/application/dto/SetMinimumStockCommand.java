package org.fernandodev.ecommerce.inventory.application.dto;

/**
 * Datos para definir el nivel mínimo de stock.
 *
 * @param minimumStock umbral de stock mínimo.
 */
public record SetMinimumStockCommand(int minimumStock) {
}