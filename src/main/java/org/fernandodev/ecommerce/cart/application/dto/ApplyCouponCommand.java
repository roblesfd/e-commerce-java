package org.fernandodev.ecommerce.cart.application.dto;

/**
 * Datos para aplicar un cupón al carrito.
 *
 * @param code código del cupón.
 */
public record ApplyCouponCommand(String code) {
}