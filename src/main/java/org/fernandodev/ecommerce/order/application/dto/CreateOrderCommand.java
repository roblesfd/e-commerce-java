package org.fernandodev.ecommerce.order.application.dto;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Datos para crear una orden (aportados por el contexto Checkout).
 */
public record CreateOrderCommand(
        UUID customerId,
        UUID addressId,
        String addressSnapshot,
        String couponCode,
        List<OrderItemCommand> items,
        Money subtotal,
        Money discounts,
        Money total
) {

    public CreateOrderCommand {
        items = items == null ? List.of() : items;
    }
}