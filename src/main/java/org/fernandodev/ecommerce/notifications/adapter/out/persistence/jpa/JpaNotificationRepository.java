package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationJpaEntity;
import org.fernandodev.ecommerce.notifications.adapter.util.mapper.NotificationPersistenceMapper;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationRepository;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link NotificationRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaNotificationRepository implements NotificationRepository {

    private final NotificationSpringDataRepository springData;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity saved = springData.save(NotificationPersistenceMapper.toEntity(notification));
        return NotificationPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return springData.findById(id.value()).map(NotificationPersistenceMapper::toDomain);
    }

    @Override
    public List<Notification> findByCustomerId(UUID customerId, boolean unreadOnly) {
        List<NotificationJpaEntity> entities = unreadOnly
                ? springData.findByCustomerIdAndReadFalseOrderByCreatedAtDesc(customerId)
                : springData.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return entities.stream().map(NotificationPersistenceMapper::toDomain).toList();
    }

    @Override
    public void delete(NotificationId id) {
        springData.deleteById(id.value());
    }

    @Override
    public boolean existsByDedupKey(String dedupKey) {
        return springData.existsByDedupKey(dedupKey);
    }
}