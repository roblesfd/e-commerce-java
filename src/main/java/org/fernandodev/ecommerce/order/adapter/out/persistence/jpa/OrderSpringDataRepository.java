package org.fernandodev.ecommerce.order.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link OrderJpaEntity}.
 */
public interface OrderSpringDataRepository extends JpaRepository<OrderJpaEntity, UUID> {

    /**
     * @param customerId identificador del cliente.
     * @return las órdenes del cliente ordenadas por fecha de creación descendente.
     */
    List<OrderJpaEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<OrderJpaEntity> findAllByOrderByCreatedAtDesc();
}