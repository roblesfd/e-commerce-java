package org.fernandodev.ecommerce.checkout.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.checkout.adapter.in.rest.dto.CheckoutResponse;
import org.fernandodev.ecommerce.checkout.adapter.in.rest.dto.StartCheckoutRequest;
import org.fernandodev.ecommerce.checkout.application.dto.StartCheckoutCommand;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio del contexto
 * Checkout. Sin lógica de negocio.
 */
@Component
public class CheckoutMapper {

    public StartCheckoutCommand toCommand(StartCheckoutRequest request) {
        return new StartCheckoutCommand(request.addressId());
    }

    public CheckoutResponse toResponse(Checkout checkout) {
        return new CheckoutResponse(
                checkout.getId().value(),
                checkout.getCustomerId(),
                checkout.getCartId(),
                checkout.getAddressId(),
                checkout.getAddressSnapshot(),
                checkout.getItems().stream().map(this::toItemResponse).toList(),
                checkout.getCouponCode(),
                toMoney(checkout.getSubtotal()),
                toMoney(checkout.getDiscounts()),
                toMoney(checkout.getTotal()),
                checkout.getStatus().name(),
                checkout.getOrderId() == null ? null : checkout.getOrderId().value(),
                checkout.getPaymentId(),
                checkout.getCreatedAt(),
                checkout.getUpdatedAt());
    }

    private CheckoutResponse.CheckoutItemResponse toItemResponse(CheckoutItem item) {
        return new CheckoutResponse.CheckoutItemResponse(
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.quantity(),
                toMoney(item.unitPrice()));
    }

    private static CheckoutResponse.MoneyResponse toMoney(Money money) {
        return new CheckoutResponse.MoneyResponse(money.amount(), money.currencyCode());
    }
}