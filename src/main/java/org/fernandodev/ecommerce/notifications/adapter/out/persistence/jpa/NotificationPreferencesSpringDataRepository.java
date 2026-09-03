package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationPreferencesJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link NotificationPreferencesJpaEntity}.
 */
public interface NotificationPreferencesSpringDataRepository
        extends JpaRepository<NotificationPreferencesJpaEntity, UUID> {

    Optional<NotificationPreferencesJpaEntity> findByCustomerId(UUID customerId);
}