package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link ShipmentJpaEntity}.
 */
public interface ShipmentSpringDataRepository extends JpaRepository<ShipmentJpaEntity, UUID> {

    Optional<ShipmentJpaEntity> findByOrderId(UUID orderId);

    Optional<ShipmentJpaEntity> findByTrackingNumber(String trackingNumber);

    List<ShipmentJpaEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}