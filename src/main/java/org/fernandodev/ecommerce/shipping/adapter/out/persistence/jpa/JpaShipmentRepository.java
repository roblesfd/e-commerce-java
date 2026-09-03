package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import org.fernandodev.ecommerce.shipping.adapter.util.mapper.ShipmentPersistenceMapper;
import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentRepository;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link ShipmentRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaShipmentRepository implements ShipmentRepository {

    private final ShipmentSpringDataRepository springData;

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentJpaEntity saved = springData.save(ShipmentPersistenceMapper.toEntity(shipment));
        return ShipmentPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Shipment> findById(ShipmentId id) {
        return springData.findById(id.value()).map(ShipmentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderId(OrderId orderId) {
        return springData.findByOrderId(orderId.value()).map(ShipmentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return springData.findByTrackingNumber(trackingNumber).map(ShipmentPersistenceMapper::toDomain);
    }

    @Override
    public List<Shipment> findByCustomerId(UUID customerId) {
        return springData.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(ShipmentPersistenceMapper::toDomain)
                .toList();
    }
}