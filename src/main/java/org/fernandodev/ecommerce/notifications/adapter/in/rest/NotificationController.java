package org.fernandodev.ecommerce.notifications.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.CreateNotificationRequest;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.NotificationResponse;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.mapper.NotificationMapper;
import org.fernandodev.ecommerce.notifications.application.port.in.NotificationUseCases;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de notificaciones.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationUseCases notificationUseCases;
    private final NotificationMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return mapper.toResponse(notificationUseCases.create(request.customerId(), mapper.toCommand(request)));
    }

    @GetMapping
    /**
     * Mine.
     * @param false false.
     * @param unread unread.
     */
    public List<NotificationResponse> mine(@RequestParam(required = false, defaultValue = "false") boolean unread) {
        return notificationUseCases.listByCustomer(SecurityUtils.requiredUserId(), unread).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public NotificationResponse get(@PathVariable UUID id) {
        return mapper.toResponse(notificationUseCases.getById(NotificationId.of(id)));
    }

    @PostMapping("/{id}/read")
    /**
     * Mark read.
     * @param id id.
     */
    public NotificationResponse markRead(@PathVariable UUID id) {
        return mapper.toResponse(notificationUseCases.markRead(NotificationId.of(id)));
    }

    @PostMapping("/{id}/unread")
    /**
     * Mark unread.
     * @param id id.
     */
    public NotificationResponse markUnread(@PathVariable UUID id) {
        return mapper.toResponse(notificationUseCases.markUnread(NotificationId.of(id)));
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Mark all read.
     */
    public void markAllRead() {
        notificationUseCases.markAllRead(SecurityUtils.requiredUserId());
    }

    @PostMapping("/{id}/retry")
    /**
     * Retry.
     * @param id id.
     */
    public NotificationResponse retry(@PathVariable UUID id) {
        return mapper.toResponse(notificationUseCases.retry(NotificationId.of(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Delete.
     * @param id id.
     */
    public void delete(@PathVariable UUID id) {
        notificationUseCases.delete(NotificationId.of(id));
    }
}