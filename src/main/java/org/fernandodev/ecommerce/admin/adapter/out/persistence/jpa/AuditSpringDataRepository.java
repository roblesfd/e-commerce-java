package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity.AuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de la auditoría.
 */
public interface AuditSpringDataRepository extends JpaRepository<AuditJpaEntity, UUID> {

    List<AuditJpaEntity> findByAction(String action);

    List<AuditJpaEntity> findByActorId(UUID actorId);

    List<AuditJpaEntity> findAllByOrderByCreatedAtDesc();
}