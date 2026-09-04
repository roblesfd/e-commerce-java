package org.fernandodev.ecommerce.notifications.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.CreateTemplateRequest;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.TemplateResponse;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.UpdateTemplateRequest;
import org.fernandodev.ecommerce.notifications.application.dto.UpdateTemplateCommand;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link NotificationTemplate} y sus DTOs REST.
 */
@Component
public class TemplateMapper {

    /**
     * @param request petición de actualización.
     * @return comando de aplicación equivalente.
     */
    public UpdateTemplateCommand toUpdateCommand(UpdateTemplateRequest request) {
        return new UpdateTemplateCommand(request.title(), request.body());
    }

    /**
     * @param template agregado de dominio.
     * @return respuesta REST con la plantilla.
     */
    public TemplateResponse toResponse(NotificationTemplate template) {
        return new TemplateResponse(
                template.getId().value(),
                template.getCode(),
                template.getTitle(),
                template.getBody(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt());
    }
}