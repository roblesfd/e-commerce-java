package org.fernandodev.ecommerce.admin.adapter.util.mapper;

import org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity.AuditJpaEntity;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;

/**
 * Mapeo entre el agregado {@link AuditEntry} y su entidad JPA.
 */
public final class AuditPersistenceMapper {

    private AuditPersistenceMapper() {
    }

    public static AuditJpaEntity toEntity(AuditEntry entry) {
        AuditJpaEntity entity = new AuditJpaEntity();
        entity.setId(entry.getId().value());
        entity.setActorId(entry.getActorId());
        entity.setAction(entry.getAction());
        entity.setTargetType(entry.getTargetType());
        entity.setTargetId(entry.getTargetId());
        entity.setDetail(entry.getDetail());
        entity.setCreatedAt(entry.getCreatedAt());
        return entity;
    }

    public static AuditEntry toDomain(AuditJpaEntity entity) {
        return AuditEntry.crear(AuditEntryId.of(entity.getId()), entity.getActorId(), entity.getAction(),
                entity.getTargetType(), entity.getTargetId(), entity.getDetail(), entity.getCreatedAt());
    }
}