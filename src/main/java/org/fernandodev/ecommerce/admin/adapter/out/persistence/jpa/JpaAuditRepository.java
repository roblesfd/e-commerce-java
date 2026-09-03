package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity.AuditJpaEntity;
import org.fernandodev.ecommerce.admin.adapter.util.mapper.AuditPersistenceMapper;
import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link AuditRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaAuditRepository implements AuditRepository {

    private final AuditSpringDataRepository springData;

    @Override
    public AuditEntry save(AuditEntry entry) {
        AuditJpaEntity saved = springData.save(AuditPersistenceMapper.toEntity(entry));
        return AuditPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<AuditEntry> findAll() {
        return springData.findAllByOrderByCreatedAtDesc().stream()
                .map(AuditPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditEntry> findByAction(String action) {
        return springData.findByAction(action).stream()
                .map(AuditPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditEntry> findByActorId(UUID actorId) {
        return springData.findByActorId(actorId).stream()
                .map(AuditPersistenceMapper::toDomain)
                .toList();
    }
}