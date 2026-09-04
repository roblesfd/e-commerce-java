package org.fernandodev.ecommerce.notifications.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.notifications.application.dto.UpdateTemplateCommand;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;

/**
 * Casos de uso de plantillas de notificación.
 */
public interface TemplateUseCases {

    /**
     * Crea una plantilla de notificación.
     */
    NotificationTemplate create(String code, String title, String body);

    /**
     * Lista todas las plantillas.
     */
    List<NotificationTemplate> list();

    /**
     * Consulta una plantilla por identificador.
     */
    NotificationTemplate getById(NotificationTemplateId id);

    /**
     * Actualiza una plantilla.
     */
    NotificationTemplate update(NotificationTemplateId id, UpdateTemplateCommand command);

    /**
     * Activa una plantilla.
     */
    NotificationTemplate activate(NotificationTemplateId id);

    /**
     * Desactiva una plantilla.
     */
    NotificationTemplate deactivate(NotificationTemplateId id);
}