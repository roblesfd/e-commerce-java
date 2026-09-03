package org.fernandodev.ecommerce.notifications.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;

/**
 * Puerto de salida de persistencia del agregado {@link NotificationTemplate}.
 */
public interface TemplateRepository {

    NotificationTemplate save(NotificationTemplate template);

    Optional<NotificationTemplate> findById(NotificationTemplateId id);

    Optional<NotificationTemplate> findByCode(String code);

    List<NotificationTemplate> findAll();
}