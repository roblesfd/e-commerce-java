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

    /**
     * @param request petición de inicio de checkout.
     * @return comando de aplicación equivalente.
     */
    public StartCheckoutCommand toCommand(StartCheckoutRequest request) {
        return new StartCheckoutCommand(request.addressId());
    }

    /**
     * @param checkout agregado de dominio.
     * @return respuesta REST con el estado del checkout.
     */
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

    /**
     * @param item item de dominio.
     * @return respuesta REST con el item.
     */
    private CheckoutResponse.CheckoutItemResponse toItemResponse(CheckoutItem item) {
        return new CheckoutResponse.CheckoutItemResponse(
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.quantity(),
                toMoney(item.unitPrice()));
    }

    /**
     * @param money importe de dominio.
     * @return respuesta REST con el importe.
     */
    private static CheckoutResponse.MoneyResponse toMoney(Money money) {
        return new CheckoutResponse.MoneyResponse(money.amount(), money.currencyCode());
    }
}