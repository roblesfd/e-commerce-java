package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity.SystemConfigJpaEntity;
import org.fernandodev.ecommerce.admin.adapter.util.mapper.SystemConfigPersistenceMapper;
import org.fernandodev.ecommerce.admin.application.port.out.SystemConfigRepository;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link SystemConfigRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaSystemConfigRepository implements SystemConfigRepository {

    private final SystemConfigSpringDataRepository springData;

    @Override
    public SystemConfig save(SystemConfig config) {
        SystemConfigJpaEntity saved = springData.save(SystemConfigPersistenceMapper.toEntity(config));
        return SystemConfigPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<SystemConfig> findById(String key) {
        return springData.findById(key).map(SystemConfigPersistenceMapper::toDomain);
    }

    @Override
    public List<SystemConfig> findAll() {
        return springData.findAll().stream()
                .map(SystemConfigPersistenceMapper::toDomain)
                .toList();
    }
}