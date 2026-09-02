package org.fernandodev.ecommerce.checkout.adapter.out;

import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.port.in.CartUseCases;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.checkout.application.dto.CartData;
import org.fernandodev.ecommerce.checkout.application.port.out.CartProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link CartProvider} consultando el contexto
 * Shopping Cart.
 */
@Component
@RequiredArgsConstructor
public class CartCartProvider implements CartProvider {

    private final CartUseCases cartUseCases;

    @Override
    public CartData getCart(UUID customerId) {
        Cart cart = cartUseCases.getCart(customerId);
        return new CartData(
                cart.getId().value(),
                cart.getItems().stream()
                        .map(item -> new CartData.CartItemData(
                                item.productId(), item.variantId(), item.quantity(), item.unitPrice()))
                        .toList(),
                cart.getCoupon() == null
                        ? null
                        : new CartData.AppliedCouponData(cart.getCoupon().code(), cart.getCoupon().discount()));
    }
}