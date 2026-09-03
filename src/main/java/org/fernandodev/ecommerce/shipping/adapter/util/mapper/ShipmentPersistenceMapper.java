package org.fernandodev.ecommerce.shipping.adapter.util.mapper;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.ShipmentStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatusEntry;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Shipment} y la entidad JPA
 * {@link ShipmentJpaEntity}. Sin lógica de negocio.
 */
public final class ShipmentPersistenceMapper {

    private ShipmentPersistenceMapper() {
    }

    public static ShipmentJpaEntity toEntity(Shipment shipment) {
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.setId(shipment.getId().value());
        entity.setOrderId(shipment.getOrderId().value());
        entity.setCustomerId(shipment.getCustomerId());
        entity.setShippingMethodId(shipment.getShippingMethodId().value());
        entity.setAddressId(shipment.getAddressId());
        entity.setAddressSnapshot(shipment.getAddressSnapshot());
        entity.setTrackingNumber(shipment.getTrackingNumber());
        entity.setStatus(shipment.getStatus());
        entity.setCostAmount(shipment.getCost().amount());
        entity.setCurrency(shipment.getCost().currencyCode());
        entity.setCreatedAt(shipment.getCreatedAt());
        shipment.getStatusHistory().forEach(entry -> entity.getStatusHistory().add(toHistoryEmbeddable(entry)));
        return entity;
    }

    public static Shipment toDomain(ShipmentJpaEntity entity) {
        return Shipment.crear(
                ShipmentId.of(entity.getId()),
                OrderId.of(entity.getOrderId()),
                entity.getCustomerId(),
                ShippingMethodId.of(entity.getShippingMethodId()),
                entity.getAddressId(),
                entity.getAddressSnapshot(),
                entity.getTrackingNumber(),
                Money.of(entity.getCostAmount(), entity.getCurrency()),
                entity.getStatus(),
                entity.getStatusHistory().stream().map(ShipmentPersistenceMapper::toDomainEntry).toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }

    private static ShipmentStatusHistoryEmbeddable toHistoryEmbeddable(ShipmentStatusEntry entry) {
        ShipmentStatusHistoryEmbeddable embeddable = new ShipmentStatusHistoryEmbeddable();
        embeddable.setStatus(entry.status());
        embeddable.setAt(entry.at());
        return embeddable;
    }

    private static ShipmentStatusEntry toDomainEntry(ShipmentStatusHistoryEmbeddable embeddable) {
        return new ShipmentStatusEntry(embeddable.getStatus(), embeddable.getAt());
    }
}