package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationRepository;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaNotificationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaNotificacion() {
        UUID customerId = UUID.randomUUID();
        Notification notification = Notification.create(NotificationId.generate(), customerId,
                NotificationChannel.IN_APP, "Título", "Cuerpo", null, AT);

        repository.save(notification);
        Notification loaded = repository.findById(notification.getId()).orElseThrow();

        assertThat(loaded.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(loaded.getChannel()).isEqualTo(NotificationChannel.IN_APP);
    }

    @Test
    void findByCustomerIdFiltraNoLeidas() {
        UUID customerId = UUID.randomUUID();
        Notification a = Notification.create(NotificationId.generate(), customerId,
                NotificationChannel.IN_APP, "A", "1", null, AT);
        a.markProcessing(AT);
        a.markSent(AT);
        a.markRead(AT);
        Notification b = Notification.create(NotificationId.generate(), customerId,
                NotificationChannel.IN_APP, "B", "2", null, AT);
        repository.save(a);
        repository.save(b);

        assertThat(repository.findByCustomerId(customerId, false)).hasSize(2);
        assertThat(repository.findByCustomerId(customerId, true)).hasSize(1);
    }

    @Test
    void existsByDedupKeyDevuelveTrue() {
        repository.save(Notification.create(NotificationId.generate(), UUID.randomUUID(),
                NotificationChannel.EMAIL, "T", "B", "EVT-1", AT));

        assertThat(repository.existsByDedupKey("EVT-1")).isTrue();
        assertThat(repository.existsByDedupKey("EVT-2")).isFalse();
    }

    @Test
    void deleteEliminaLaNotificacion() {
        Notification notification = Notification.create(NotificationId.generate(), UUID.randomUUID(),
                NotificationChannel.IN_APP, "T", "B", null, AT);
        repository.save(notification);

        repository.delete(notification.getId());

        assertThat(repository.findById(notification.getId())).isEmpty();
    }
}