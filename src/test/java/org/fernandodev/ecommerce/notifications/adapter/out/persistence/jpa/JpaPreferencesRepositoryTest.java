package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.notifications.application.port.out.PreferencesRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationPreferences;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaPreferencesRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PreferencesRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaPreferencias() {
        UUID customerId = UUID.randomUUID();
        NotificationPreferences preferences = NotificationPreferences.create(customerId, AT);
        preferences.setEnabled(NotificationChannel.EMAIL, false, AT);

        repository.save(preferences);
        NotificationPreferences loaded = repository.findByCustomerId(customerId).orElseThrow();

        assertThat(loaded.isEnabled(NotificationChannel.EMAIL)).isFalse();
        assertThat(loaded.isEnabled(NotificationChannel.SMS)).isTrue();
    }

    @Test
    void findByCustomerIdInexistenteDevuelveVacio() {
        assertThat(repository.findByCustomerId(UUID.randomUUID())).isEmpty();
    }
}