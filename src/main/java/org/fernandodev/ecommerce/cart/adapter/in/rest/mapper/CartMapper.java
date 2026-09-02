package org.fernandodev.ecommerce.cart.adapter.in.rest.mapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.AddItemRequest;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.ApplyCouponRequest;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.CartResponse;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.TotalsResponse;
import org.fernandodev.ecommerce.cart.adapter.in.rest.dto.UpdateQuantityRequest;
import org.fernandodev.ecommerce.cart.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.cart.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.cart.application.dto.UpdateQuantityCommand;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.cart.domain.value_object.AppliedCoupon;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItem;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio del contexto
 * Shopping Cart. Sin lógica de negocio.
 */
@Component
public class CartMapper {

    public AddItemCommand toCommand(AddItemRequest request) {
        return new AddItemCommand(
                ProductId.of(request.productId()),
                request.variantId() == null ? null : ProductVariantId.of(request.variantId()),
                request.quantity());
    }

    public UpdateQuantityCommand toCommand(UpdateQuantityRequest request) {
        return new UpdateQuantityCommand(request.quantity());
    }

    public ApplyCouponCommand toCommand(ApplyCouponRequest request) {
        return new ApplyCouponCommand(request.code());
    }

    public CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId().value(),
                cart.getItems().stream().map(this::toItemResponse).toList(),
                cart.getCoupon() == null ? null : toCouponResponse(cart.getCoupon()),
                toMoneyResponse(cart.subtotal()),
                toMoneyResponse(cart.discount()),
                toMoneyResponse(cart.total()));
    }

    public TotalsResponse toTotalsResponse(Optional<Money> subtotal, Optional<Money> discounts, Optional<Money> total) {
        return new TotalsResponse(toMoneyResponse(subtotal), toMoneyResponse(discounts), toMoneyResponse(total));
    }

    private CartResponse.CartItemResponse toItemResponse(CartItem item) {
        return new CartResponse.CartItemResponse(
                item.id().value(),
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.quantity(),
                toMoneyResponse(item.unitPrice()));
    }

    private static CartResponse.AppliedCouponResponse toCouponResponse(AppliedCoupon coupon) {
        return new CartResponse.AppliedCouponResponse(
                coupon.code(),
                toMoneyResponse(coupon.discount()));
    }

    private static CartResponse.MoneyResponse toMoneyResponse(Money money) {
        return money == null ? null : new CartResponse.MoneyResponse(money.amount(), money.currencyCode());
    }

    private static CartResponse.MoneyResponse toMoneyResponse(Optional<Money> money) {
        return money.map(CartMapper::toMoneyResponse).orElse(null);
    }
}