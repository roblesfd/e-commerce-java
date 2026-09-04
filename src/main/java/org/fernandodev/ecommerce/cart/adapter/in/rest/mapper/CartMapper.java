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

    /**
     * @param request petición de alta de item.
     * @return comando de aplicación equivalente.
     */
    public AddItemCommand toCommand(AddItemRequest request) {
        return new AddItemCommand(
                ProductId.of(request.productId()),
                request.variantId() == null ? null : ProductVariantId.of(request.variantId()),
                request.quantity());
    }

    /**
     * @param request petición de actualización de cantidad.
     * @return comando de aplicación equivalente.
     */
    public UpdateQuantityCommand toCommand(UpdateQuantityRequest request) {
        return new UpdateQuantityCommand(request.quantity());
    }

    /**
     * @param request petición de aplicación de cupón.
     * @return comando de aplicación equivalente.
     */
    public ApplyCouponCommand toCommand(ApplyCouponRequest request) {
        return new ApplyCouponCommand(request.code());
    }

    /**
     * @param cart agregado de dominio.
     * @return respuesta REST con el estado del carrito.
     */
    public CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId().value(),
                cart.getItems().stream().map(this::toItemResponse).toList(),
                cart.getCoupon() == null ? null : toCouponResponse(cart.getCoupon()),
                toMoneyResponse(cart.subtotal()),
                toMoneyResponse(cart.discount()),
                toMoneyResponse(cart.total()));
    }

    /**
     * @param subtotal  subtotal del carrito.
     * @param discounts descuento del carrito.
     * @param total     total del carrito.
     * @return respuesta REST con los totales.
     */
    public TotalsResponse toTotalsResponse(Optional<Money> subtotal, Optional<Money> discounts, Optional<Money> total) {
        return new TotalsResponse(toMoneyResponse(subtotal), toMoneyResponse(discounts), toMoneyResponse(total));
    }

    /**
     * @param item item de dominio.
     * @return respuesta REST con el item.
     */
    private CartResponse.CartItemResponse toItemResponse(CartItem item) {
        return new CartResponse.CartItemResponse(
                item.id().value(),
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.quantity(),
                toMoneyResponse(item.unitPrice()));
    }

    /**
     * @param coupon cupón de dominio.
     * @return respuesta REST con el cupón aplicado.
     */
    private static CartResponse.AppliedCouponResponse toCouponResponse(AppliedCoupon coupon) {
        return new CartResponse.AppliedCouponResponse(
                coupon.code(),
                toMoneyResponse(coupon.discount()));
    }

    /**
     * @param money importe, o {@code null} si no hay.
     * @return respuesta REST con el importe, o {@code null}.
     */
    private static CartResponse.MoneyResponse toMoneyResponse(Money money) {
        return money == null ? null : new CartResponse.MoneyResponse(money.amount(), money.currencyCode());
    }

    /**
     * @param money importe opcional.
     * @return respuesta REST con el importe, o {@code null} si está vacío.
     */
    private static CartResponse.MoneyResponse toMoneyResponse(Optional<Money> money) {
        return money.map(CartMapper::toMoneyResponse).orElse(null);
    }
}