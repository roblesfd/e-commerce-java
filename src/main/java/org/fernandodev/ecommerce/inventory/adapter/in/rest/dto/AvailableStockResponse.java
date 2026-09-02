package org.fernandodev.ecommerce.inventory.adapter.in.rest.dto;

/**
 * Respuesta con el stock disponible de un producto/variante.
 */
public record AvailableStockResponse(int available, boolean lowStock) {
}