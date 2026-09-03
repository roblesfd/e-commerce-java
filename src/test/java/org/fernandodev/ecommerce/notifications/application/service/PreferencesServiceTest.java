package org.fernandodev.ecommerce.notifications.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.UpdatePreferencesCommand;
import org.fernandodev.ecommerce.notifications.application.port.out.PreferencesRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.junit.jupiter.api.Test;

class PreferencesServiceTest {

    private final FakePreferencesRepository repository = new FakePreferencesRepository();
    private final PreferencesService service = new PreferencesService(repository);

    @Test
    void getByCustomerCreaPreferenciasPorDefecto() {
        UUID customerId = UUID.randomUUID();

        NotificationPreferences prefs = service.getByCustomer(customerId);

        assertThat(prefs.isEnabled(NotificationChannel.EMAIL)).isTrue();
        assertThat(repository.findByCustomerId(customerId)).isPresent();
    }

    @Test
    void updateCambiaLosCanales() {
        UUID customerId = UUID.randomUUID();
        service.getByCustomer(customerId);

        NotificationPreferences updated = service.update(customerId,
                new UpdatePreferencesCommand(false, true, false, true));

        assertThat(updated.isEnabled(NotificationChannel.EMAIL)).isFalse();
        assertThat(updated.isEnabled(NotificationChannel.SMS)).isTrue();
        assertThat(updated.isEnabled(NotificationChannel.PUSH)).isFalse();
        assertThat(updated.isEnabled(NotificationChannel.IN_APP)).isTrue();
    }

    private static final class FakePreferencesRepository implements PreferencesRepository {

        private NotificationPreferences stored;

        @Override
        public NotificationPreferences save(NotificationPreferences preferences) {
            this.stored = preferences;
            return preferences;
        }

        @Override
        public Optional<NotificationPreferences> findByCustomerId(UUID customerId) {
            return Optional.ofNullable(stored);
        }
    }
}