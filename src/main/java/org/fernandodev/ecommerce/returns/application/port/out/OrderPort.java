package org.fernandodev.ecommerce.returns.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Puerto hacia el contexto Order Management: consultar la orden y su estado de
 * entrega.
 */
public interface OrderPort {

    OrderInfo getOrder(OrderId orderId);

    record OrderInfo(UUID customerId, String status, Instant deliveredAt, List<ItemInfo> items) {
    }

    record ItemInfo(UUID orderItemId, ProductId productId, int quantity, Money unitPrice) {
    }
}