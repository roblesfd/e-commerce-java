package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link NotificationTemplateJpaEntity}.
 */
public interface NotificationTemplateSpringDataRepository extends JpaRepository<NotificationTemplateJpaEntity, UUID> {

    Optional<NotificationTemplateJpaEntity> findByCode(String code);

    List<NotificationTemplateJpaEntity> findAllByOrderByCreatedAtAsc();
}