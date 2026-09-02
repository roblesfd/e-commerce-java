package org.fernandodev.ecommerce.promotion.application.dto;

import java.util.List;

/**
 * Datos para validar o aplicar un cupón sobre un carrito.
 */
public record ApplyCouponCommand(String code, List<CartItemDto> items) {

    public ApplyCouponCommand {
        items = items == null ? List.of() : items;
    }
}