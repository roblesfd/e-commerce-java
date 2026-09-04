package org.fernandodev.ecommerce.admin.adapter.util.mapper;

import org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity.SystemConfigJpaEntity;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;

/**
 * Mapeo entre el agregado {@link SystemConfig} y su entidad JPA.
 */
public final class SystemConfigPersistenceMapper {

    private SystemConfigPersistenceMapper() {
    }

    /**
     * @param config configuración de dominio.
     * @return entidad JPA equivalente.
     */
    public static SystemConfigJpaEntity toEntity(SystemConfig config) {
        SystemConfigJpaEntity entity = new SystemConfigJpaEntity();
        entity.setKey(config.getKey());
        entity.setValue(config.getValue());
        return entity;
    }

    /**
     * @param entity entidad JPA.
     * @return configuración de dominio equivalente.
     */
    public static SystemConfig toDomain(SystemConfigJpaEntity entity) {
        return SystemConfig.crear(entity.getKey(), entity.getValue(),
                entity.getUpdatedAt() == null ? java.time.Instant.now() : entity.getUpdatedAt());
    }
}