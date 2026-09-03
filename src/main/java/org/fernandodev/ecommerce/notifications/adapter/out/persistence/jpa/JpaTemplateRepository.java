package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa.entity.NotificationTemplateJpaEntity;
import org.fernandodev.ecommerce.notifications.adapter.util.mapper.TemplatePersistenceMapper;
import org.fernandodev.ecommerce.notifications.application.port.out.TemplateRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link TemplateRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaTemplateRepository implements TemplateRepository {

    private final NotificationTemplateSpringDataRepository springData;

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        NotificationTemplateJpaEntity saved = springData.save(TemplatePersistenceMapper.toEntity(template));
        return TemplatePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationTemplate> findById(NotificationTemplateId id) {
        return springData.findById(id.value()).map(TemplatePersistenceMapper::toDomain);
    }

    @Override
    public Optional<NotificationTemplate> findByCode(String code) {
        return springData.findByCode(code).map(TemplatePersistenceMapper::toDomain);
    }

    @Override
    public List<NotificationTemplate> findAll() {
        return springData.findAllByOrderByCreatedAtAsc().stream()
                .map(TemplatePersistenceMapper::toDomain)
                .toList();
    }
}