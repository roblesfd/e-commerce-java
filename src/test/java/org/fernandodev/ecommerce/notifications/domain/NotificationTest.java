package org.fernandodev.ecommerce.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationChannel;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationId;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationStatus;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class NotificationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaPendingSinLeer() {
        Notification notification = notification();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getDeliveryAttempts()).isZero();
    }

    @Test
    void cicloEntregaExitosa() {
        Notification notification = notification();

        notification.markProcessing(T1);
        notification.markSent(T1);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getDeliveryAttempts()).isEqualTo(1);
        assertThat(notification.getSentAt()).isEqualTo(T1);
    }

    @Test
    void cicloEntregaFallidaYRetry() {
        Notification notification = notification();

        notification.markProcessing(T1);
        notification.markFailed(T1);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getFailedAt()).isEqualTo(T1);

        notification.retry(T1);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getFailedAt()).isNull();
    }

    @Test
    void retrySoloDesdeFallidoLanzaExcepcion() {
        Notification notification = notification();

        assertThatThrownBy(() -> notification.retry(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void markReadYUnreadSoloCuandoEnviada() {
        Notification notification = notification();
        notification.markProcessing(T1);
        notification.markSent(T1);

        notification.markRead(T1);
        assertThat(notification.isRead()).isTrue();

        notification.markUnread(T1);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void markReadDePendingLanzaExcepcion() {
        Notification notification = notification();

        assertThatThrownBy(() -> notification.markRead(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cancelDePendingPasaACancelled() {
        Notification notification = notification();

        notification.cancel(T1);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.CANCELLED);
    }

    @Test
    void cancelDeProcesandoLanzaExcepcion() {
        Notification notification = notification();
        notification.markProcessing(T1);

        assertThatThrownBy(() -> notification.cancel(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Notification notification() {
        return Notification.create(NotificationId.generate(), UUID.randomUUID(), NotificationChannel.IN_APP,
                "Pedido enviado", "Tu pedido está en camino", null, T1);
    }
}