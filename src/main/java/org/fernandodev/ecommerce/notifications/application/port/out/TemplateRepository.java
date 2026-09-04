package org.fernandodev.ecommerce.notifications.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;

/**
 * Puerto de salida de persistencia del agregado {@link NotificationTemplate}.
 */
public interface TemplateRepository {

    /**
     * Persiste la plantilla.
     */
    NotificationTemplate save(NotificationTemplate template);

    /**
     * Consulta una plantilla por identificador.
     */
    Optional<NotificationTemplate> findById(NotificationTemplateId id);

    /**
     * Consulta una plantilla por código.
     */
    Optional<NotificationTemplate> findByCode(String code);

    /**
     * Lista todas las plantillas.
     */
    List<NotificationTemplate> findAll();
}