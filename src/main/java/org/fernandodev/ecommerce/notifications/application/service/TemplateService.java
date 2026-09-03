package org.fernandodev.ecommerce.notifications.application.service;

import java.time.Instant;
import java.util.List;

import org.fernandodev.ecommerce.notifications.application.dto.UpdateTemplateCommand;
import org.fernandodev.ecommerce.notifications.application.port.in.TemplateUseCases;
import org.fernandodev.ecommerce.notifications.application.port.out.TemplateRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de plantillas de notificación.
 */
@RequiredArgsConstructor
public class TemplateService implements TemplateUseCases {

    private final TemplateRepository templates;

    @Override
    public NotificationTemplate create(String code, String title, String body) {
        NotificationTemplate template = NotificationTemplate.create(
                NotificationTemplateId.generate(), code, title, body, Instant.now());
        return templates.save(template);
    }

    @Override
    public List<NotificationTemplate> list() {
        return templates.findAll();
    }

    @Override
    public NotificationTemplate getById(NotificationTemplateId id) {
        return getExisting(id);
    }

    @Override
    public NotificationTemplate update(NotificationTemplateId id, UpdateTemplateCommand command) {
        NotificationTemplate template = getExisting(id);
        template.update(command.title(), command.body(), Instant.now());
        return templates.save(template);
    }

    @Override
    public NotificationTemplate activate(NotificationTemplateId id) {
        NotificationTemplate template = getExisting(id);
        template.activate();
        return templates.save(template);
    }

    @Override
    public NotificationTemplate deactivate(NotificationTemplateId id) {
        NotificationTemplate template = getExisting(id);
        template.deactivate();
        return templates.save(template);
    }

    private NotificationTemplate getExisting(NotificationTemplateId id) {
        return templates.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + id));
    }
}