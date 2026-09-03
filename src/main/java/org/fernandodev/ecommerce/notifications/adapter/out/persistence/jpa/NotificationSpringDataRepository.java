package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link NotificationJpaEntity}.
 */
public interface NotificationSpringDataRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<NotificationJpaEntity> findByCustomerIdAndReadFalseOrderByCreatedAtDesc(UUID customerId);

    boolean existsByDedupKey(String dedupKey);
}