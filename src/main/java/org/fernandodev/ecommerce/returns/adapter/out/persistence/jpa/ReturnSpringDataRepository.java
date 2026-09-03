package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link ReturnJpaEntity}.
 */
public interface ReturnSpringDataRepository extends JpaRepository<ReturnJpaEntity, UUID> {

    Optional<ReturnJpaEntity> findByOrderId(UUID orderId);

    List<ReturnJpaEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    boolean existsByOrderId(UUID orderId);

    List<ReturnJpaEntity> findAllByOrderByCreatedAtDesc();
}