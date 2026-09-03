package org.fernandodev.ecommerce.notifications.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.notifications.application.dto.UpdateTemplateCommand;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;

/**
 * Casos de uso de plantillas de notificación.
 */
public interface TemplateUseCases {

    NotificationTemplate create(String code, String title, String body);

    List<NotificationTemplate> list();

    NotificationTemplate getById(NotificationTemplateId id);

    NotificationTemplate update(NotificationTemplateId id, UpdateTemplateCommand command);

    NotificationTemplate activate(NotificationTemplateId id);

    NotificationTemplate deactivate(NotificationTemplateId id);
}