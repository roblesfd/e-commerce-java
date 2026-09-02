package org.fernandodev.ecommerce.cart.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.cart.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.cart.application.dto.UpdateQuantityCommand;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItemId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Casos de uso del contexto Shopping Cart (puerto de entrada).
 */
public interface CartUseCases {

    Cart createCart(UUID ownerId);

    Cart getCart(UUID ownerId);

    Cart addItem(UUID ownerId, AddItemCommand command);

    Cart updateItemQuantity(UUID ownerId, CartItemId itemId, UpdateQuantityCommand command);

    Cart removeItem(UUID ownerId, CartItemId itemId);

    Cart clearCart(UUID ownerId);

    Cart applyCoupon(UUID ownerId, ApplyCouponCommand command);

    Cart removeCoupon(UUID ownerId);

    Optional<Money> subtotal(UUID ownerId);

    Optional<Money> discount(UUID ownerId);

    Optional<Money> total(UUID ownerId);

    Cart validateCart(UUID ownerId);

    void expireCart(UUID ownerId);
}