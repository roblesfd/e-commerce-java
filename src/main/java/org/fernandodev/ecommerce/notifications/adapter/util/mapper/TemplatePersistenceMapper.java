package org.fernandodev.ecommerce.notifications.adapter.util.mapper;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationTemplateJpaEntity;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;

/**
 * Mapeo entre el agregado {@link NotificationTemplate} y su entidad JPA.
 */
public final class TemplatePersistenceMapper {

    private TemplatePersistenceMapper() {
    }

    /**
     * @param template agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static NotificationTemplateJpaEntity toEntity(NotificationTemplate template) {
        NotificationTemplateJpaEntity entity = new NotificationTemplateJpaEntity();
        entity.setId(template.getId().value());
        entity.setCode(template.getCode());
        entity.setTitle(template.getTitle());
        entity.setBody(template.getBody());
        entity.setActive(template.isActive());
        entity.setCreatedAt(template.getCreatedAt());
        return entity;
    }

    /**
     * @param notificationTemplateJpaEntity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static NotificationTemplate toDomain(NotificationTemplateJpaEntity entity) {
        return NotificationTemplate.crear(
                NotificationTemplateId.of(entity.getId()),
                entity.getCode(),
                entity.getTitle(),
                entity.getBody(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }
}