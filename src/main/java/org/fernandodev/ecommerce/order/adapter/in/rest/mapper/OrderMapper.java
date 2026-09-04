package org.fernandodev.ecommerce.order.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.order.adapter.in.rest.dto.OrderResponse;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItem;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre el agregado {@link Order} y el DTO REST.
 */
@Component
public class OrderMapper {

    /**
     * @param order agregado de dominio.
     * @return respuesta REST con el detalle de la orden.
     */
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId().value(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getItems().stream().map(this::toItemResponse).toList(),
                order.getAddressId(),
                order.getAddressSnapshot(),
                order.getCouponCode(),
                toMoney(order.getSubtotal()),
                toMoney(order.getDiscounts()),
                toMoney(order.getTotal()),
                order.getStatusHistory().stream()
                        .map(entry -> new OrderResponse.StatusEntryResponse(
                                entry.status().name(), entry.at()))
                        .toList(),
                order.getCreatedAt());
    }

    /**
     * @param item item de dominio.
     * @return respuesta REST con el item.
     */
    private OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderResponse.OrderItemResponse(
                item.id().value(),
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.sku(),
                item.name(),
                item.quantity(),
                toMoney(item.unitPrice()),
                toMoney(item.subtotal()));
    }

    /**
     * @param money importe de dominio.
     * @return respuesta REST con el importe.
     */
    private static OrderResponse.MoneyResponse toMoney(Money money) {
        return new OrderResponse.MoneyResponse(money.amount(), money.currencyCode());
    }
}