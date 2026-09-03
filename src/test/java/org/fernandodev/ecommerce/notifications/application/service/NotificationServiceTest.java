package org.fernandodev.ecommerce.notifications.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.application.dto.CreateNotificationCommand;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationRepository;
import org.fernandodev.ecommerce.notifications.domain.Notification;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationStatus;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private final FakeNotificationRepository repository = new FakeNotificationRepository();
    private final FakeNotificationProvider provider = new FakeNotificationProvider();
    private final NotificationService service = new NotificationService(repository, provider);

    @Test
    void createInAppEntregaInmediatamente() {
        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.IN_APP, "Título", "Cuerpo", null));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.isRead()).isFalse();
        assertThat(provider.sendCalls).isZero();
    }

    @Test
    void createEmailLlamaAlProveedorYEnvia() {
        provider.result = NotificationProviderPort.DeliveryResult.ok();

        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.EMAIL, "Título", "Cuerpo", null));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(provider.sendCalls).isEqualTo(1);
        assertThat(notification.getDeliveryAttempts()).isEqualTo(1);
    }

    @Test
    void createConFallosDeProveedorMarcaFailed() {
        provider.result = NotificationProviderPort.DeliveryResult.fail();

        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.SMS, "Título", "Cuerpo", null));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getFailedAt()).isNotNull();
    }

    @Test
    void createConExcepcionDelProveedorMarcaFailed() {
        provider.exception = true;

        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.PUSH, "Título", "Cuerpo", null));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void createConDedupDuplicadoLanzaConflicto() {
        repository.duplicado = true;

        assertThatThrownBy(() -> service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.IN_APP, "Título", "Cuerpo", "EVT-1")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void retryReintentaNotificacionFallida() {
        provider.result = NotificationProviderPort.DeliveryResult.fail();
        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.EMAIL, "Título", "Cuerpo", null));
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);

        provider.result = NotificationProviderPort.DeliveryResult.ok();
        Notification retried = service.retry(notification.getId());

        assertThat(retried.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(retried.getDeliveryAttempts()).isEqualTo(2);
    }

    @Test
    void retryDeNoFallidaLanzaViolacion() {
        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.IN_APP, "Título", "Cuerpo", null));

        assertThatThrownBy(() -> service.retry(notification.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void markReadYUnreadCambianElEstado() {
        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.IN_APP, "Título", "Cuerpo", null));

        Notification read = service.markRead(notification.getId());
        assertThat(read.isRead()).isTrue();

        Notification unread = service.markUnread(notification.getId());
        assertThat(unread.isRead()).isFalse();
    }

    @Test
    void markAllReadMarcaTodasLasNoLeidas() {
        UUID customerId = UUID.randomUUID();
        service.create(customerId, new CreateNotificationCommand(NotificationChannel.IN_APP, "A", "1", null));
        service.create(customerId, new CreateNotificationCommand(NotificationChannel.IN_APP, "B", "2", null));

        service.markAllRead(customerId);

        assertThat(service.listByCustomer(customerId, true)).isEmpty();
        assertThat(service.listByCustomer(customerId, false)).hasSize(2)
                .allMatch(Notification::isRead);
    }

    @Test
    void deleteEliminaLaNotificacion() {
        Notification notification = service.create(UUID.randomUUID(),
                new CreateNotificationCommand(NotificationChannel.IN_APP, "Título", "Cuerpo", null));

        service.delete(notification.getId());

        assertThat(repository.findById(notification.getId())).isEmpty();
    }

    @Test
    void listByCustomerConFiltroUnread() {
        UUID customerId = UUID.randomUUID();
        Notification a = service.create(customerId,
                new CreateNotificationCommand(NotificationChannel.IN_APP, "A", "1", null));
        service.create(customerId, new CreateNotificationCommand(NotificationChannel.IN_APP, "B", "2", null));
        service.markRead(a.getId());

        assertThat(service.listByCustomer(customerId, true)).hasSize(1);
        assertThat(service.listByCustomer(customerId, false)).hasSize(2);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(NotificationId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static final class FakeNotificationRepository implements NotificationRepository {

        private final Map<NotificationId, Notification> byId = new HashMap<>();
        private boolean duplicado;

        @Override
        public Notification save(Notification notification) {
            byId.put(notification.getId(), notification);
            return notification;
        }

        @Override
        public Optional<Notification> findById(NotificationId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Notification> findByCustomerId(UUID customerId, boolean unreadOnly) {
            List<Notification> result = new ArrayList<>();
            byId.values().stream()
                    .filter(n -> n.getCustomerId().equals(customerId))
                    .filter(n -> !unreadOnly || !n.isRead())
                    .forEach(result::add);
            return result;
        }

        @Override
        public void delete(NotificationId id) {
            byId.remove(id);
        }

        @Override
        public boolean existsByDedupKey(String dedupKey) {
            return duplicado || byId.values().stream()
                    .anyMatch(n -> dedupKey.equals(n.getDedupKey()));
        }
    }

    private static final class FakeNotificationProvider implements NotificationProviderPort {
        private DeliveryResult result = DeliveryResult.ok();
        private boolean exception;
        private int sendCalls;

        @Override
        public DeliveryResult send(NotificationChannel channel, UUID customerId, String title, String body) {
            sendCalls++;
            if (exception) {
                throw new IllegalStateException("provider caído");
            }
            return result;
        }
    }
}