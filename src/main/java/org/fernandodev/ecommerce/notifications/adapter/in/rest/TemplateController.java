package org.fernandodev.ecommerce.notifications.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.CreateTemplateRequest;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.TemplateResponse;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.UpdateTemplateRequest;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.mapper.TemplateMapper;
import org.fernandodev.ecommerce.notifications.application.port.in.TemplateUseCases;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de plantillas de notificación.
 */
@RestController
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateUseCases templateUseCases;
    private final TemplateMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
        return mapper.toResponse(templateUseCases.create(request.code(), request.title(), request.body()));
    }

    @GetMapping
    public List<TemplateResponse> list() {
        return templateUseCases.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public TemplateResponse get(@PathVariable UUID id) {
        return mapper.toResponse(templateUseCases.getById(NotificationTemplateId.of(id)));
    }

    @PutMapping("/{id}")
    public TemplateResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTemplateRequest request) {
        return mapper.toResponse(templateUseCases.update(NotificationTemplateId.of(id), mapper.toUpdateCommand(request)));
    }

    @PostMapping("/{id}/activate")
    public TemplateResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(templateUseCases.activate(NotificationTemplateId.of(id)));
    }

    @PostMapping("/{id}/deactivate")
    public TemplateResponse deactivate(@PathVariable UUID id) {
        return mapper.toResponse(templateUseCases.deactivate(NotificationTemplateId.of(id)));
    }
}