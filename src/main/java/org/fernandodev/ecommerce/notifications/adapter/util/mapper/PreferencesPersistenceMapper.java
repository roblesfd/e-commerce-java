package org.fernandodev.ecommerce.notifications.adapter.util.mapper;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationPreferencesJpaEntity;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;

/**
 * Mapeo entre el agregado {@link NotificationPreferences} y su entidad JPA.
 */
public final class PreferencesPersistenceMapper {

    private PreferencesPersistenceMapper() {
    }

    /**
     * @param preferences agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static NotificationPreferencesJpaEntity toEntity(NotificationPreferences preferences) {
        NotificationPreferencesJpaEntity entity = new NotificationPreferencesJpaEntity();
        entity.setCustomerId(preferences.getCustomerId());
        entity.setEmailEnabled(preferences.isEmailEnabled());
        entity.setSmsEnabled(preferences.isSmsEnabled());
        entity.setPushEnabled(preferences.isPushEnabled());
        entity.setInAppEnabled(preferences.isInAppEnabled());
        return entity;
    }

    /**
     * @param notificationPreferencesJpaEntity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static NotificationPreferences toDomain(NotificationPreferencesJpaEntity entity) {
        return NotificationPreferences.crear(
                entity.getCustomerId(),
                entity.isEmailEnabled(),
                entity.isSmsEnabled(),
                entity.isPushEnabled(),
                entity.isInAppEnabled(),
                entity.getUpdatedAt() == null ? java.time.Instant.now() : entity.getUpdatedAt());
    }
}