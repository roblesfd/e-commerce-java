package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationPreferencesJpaEntity;
import org.fernandodev.ecommerce.notifications.adapter.util.mapper.PreferencesPersistenceMapper;
import org.fernandodev.ecommerce.notifications.application.port.out.PreferencesRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link PreferencesRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaPreferencesRepository implements PreferencesRepository {

    private final NotificationPreferencesSpringDataRepository springData;

    @Override
    public NotificationPreferences save(NotificationPreferences preferences) {
        NotificationPreferencesJpaEntity saved = springData.save(PreferencesPersistenceMapper.toEntity(preferences));
        return PreferencesPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationPreferences> findByCustomerId(UUID customerId) {
        return springData.findByCustomerId(customerId).map(PreferencesPersistenceMapper::toDomain);
    }
}