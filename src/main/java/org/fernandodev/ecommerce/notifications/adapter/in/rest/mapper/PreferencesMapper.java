package org.fernandodev.ecommerce.notifications.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.PreferencesResponse;
import org.fernandodev.ecommerce.notifications.adapter.in.rest.dto.UpdatePreferencesRequest;
import org.fernandodev.ecommerce.notifications.application.dto.UpdatePreferencesCommand;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link NotificationPreferences} y sus DTOs REST.
 */
@Component
public class PreferencesMapper {

    public UpdatePreferencesCommand toCommand(UpdatePreferencesRequest request) {
        return new UpdatePreferencesCommand(
                request.emailEnabled(), request.smsEnabled(), request.pushEnabled(), request.inAppEnabled());
    }

    public PreferencesResponse toResponse(NotificationPreferences preferences) {
        return new PreferencesResponse(
                preferences.getCustomerId(),
                preferences.isEmailEnabled(),
                preferences.isSmsEnabled(),
                preferences.isPushEnabled(),
                preferences.isInAppEnabled(),
                preferences.getUpdatedAt());
    }
}