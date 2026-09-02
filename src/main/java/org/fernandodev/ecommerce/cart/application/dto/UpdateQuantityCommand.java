package org.fernandodev.ecommerce.cart.application.dto;

/**
 * Datos para actualizar la cantidad de un item.
 *
 * @param quantity nueva cantidad.
 */
public record UpdateQuantityCommand(int quantity) {
}