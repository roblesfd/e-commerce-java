package org.fernandodev.ecommerce.order.adapter.util.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.order.adapter.out.persistence.jpa.OrderStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.order.adapter.out.persistence.jpa.entity.OrderItemJpaEntity;
import org.fernandodev.ecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItem;
import org.fernandodev.ecommerce.order.domain.value_object.OrderItemId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderStatusEntry;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Order} y la entidad JPA
 * {@link OrderJpaEntity}. Sin lógica de negocio.
 */
public final class OrderPersistenceMapper {

    private OrderPersistenceMapper() {
    }

    public static OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId().value());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setAddressId(order.getAddressId());
        entity.setAddressSnapshot(order.getAddressSnapshot());
        entity.setCouponCode(order.getCouponCode());
        entity.setSubtotalAmount(order.getSubtotal().amount());
        entity.setDiscountsAmount(order.getDiscounts().amount());
        entity.setTotalAmount(order.getTotal().amount());
        entity.setCurrency(order.getSubtotal().currencyCode());
        entity.setCreatedAt(order.getCreatedAt());
        List<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            entity.getItems().add(toItemEntity(items.get(i), i, entity));
        }
        order.getStatusHistory().forEach(entry -> entity.getStatusHistory().add(toHistoryEmbeddable(entry)));
        return entity;
    }

    public static Order toDomain(OrderJpaEntity entity) {
        return Order.crear(
                OrderId.of(entity.getId()),
                entity.getCustomerId(),
                entity.getItems().stream().map(OrderPersistenceMapper::toDomainItem).toList(),
                entity.getStatus(),
                entity.getAddressId(),
                entity.getAddressSnapshot(),
                entity.getCouponCode(),
                Money.of(entity.getSubtotalAmount(), entity.getCurrency()),
                Money.of(entity.getDiscountsAmount(), entity.getCurrency()),
                Money.of(entity.getTotalAmount(), entity.getCurrency()),
                entity.getStatusHistory().stream().map(OrderPersistenceMapper::toDomainEntry).toList(),
                entity.getCreatedAt());
    }

    private static OrderItemJpaEntity toItemEntity(OrderItem item, int position, OrderJpaEntity order) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setId(item.id().value());
        entity.setOrder(order);
        entity.setPosition(position);
        entity.setProductId(item.productId().value());
        entity.setVariantId(item.variantId() == null ? null : item.variantId().value());
        entity.setSku(item.sku());
        entity.setName(item.name());
        entity.setQuantity(item.quantity());
        entity.setUnitAmount(item.unitPrice().amount());
        entity.setUnitCurrency(item.unitPrice().currencyCode());
        entity.setSubtotalAmount(item.subtotal().amount());
        return entity;
    }

    private static OrderItem toDomainItem(OrderItemJpaEntity entity) {
        return new OrderItem(
                OrderItemId.of(entity.getId()),
                ProductId.of(entity.getProductId()),
                entity.getVariantId() == null ? null : ProductVariantId.of(entity.getVariantId()),
                entity.getSku(),
                entity.getName(),
                entity.getQuantity(),
                Money.of(entity.getUnitAmount(), entity.getUnitCurrency()));
    }

    private static OrderStatusHistoryEmbeddable toHistoryEmbeddable(OrderStatusEntry entry) {
        OrderStatusHistoryEmbeddable embeddable = new OrderStatusHistoryEmbeddable();
        embeddable.setStatus(entry.status());
        embeddable.setAt(entry.at());
        return embeddable;
    }

    private static OrderStatusEntry toDomainEntry(OrderStatusHistoryEmbeddable embeddable) {
        return new OrderStatusEntry(embeddable.getStatus(), embeddable.getAt());
    }
}