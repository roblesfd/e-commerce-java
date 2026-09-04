package org.fernandodev.ecommerce.notifications.adapter.in.rest;

import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.PreferencesResponse;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.UpdatePreferencesRequest;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.mapper.PreferencesMapper;
import org.fernandodev.ecommerce.notifications.application.port.in.PreferencesUseCases;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de preferencias de canales de notificación.
 */
@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final PreferencesUseCases preferencesUseCases;
    private final PreferencesMapper mapper;

    @GetMapping
    public PreferencesResponse get() {
        return mapper.toResponse(preferencesUseCases.getByCustomer(SecurityUtils.requiredUserId()));
    }

    @PutMapping
    /**
     * Update.
     * @param request request.
     */
    public PreferencesResponse update(@Valid @RequestBody UpdatePreferencesRequest request) {
        return mapper.toResponse(preferencesUseCases.update(SecurityUtils.requiredUserId(), mapper.toCommand(request)));
    }
}