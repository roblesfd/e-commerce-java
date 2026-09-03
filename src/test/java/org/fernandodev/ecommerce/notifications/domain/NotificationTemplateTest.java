package org.fernandodev.ecommerce.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class NotificationTemplateTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaPlantillaActiva() {
        NotificationTemplate template = NotificationTemplate.create(
                NotificationTemplateId.generate(), "ORDER_SHIPPED", "Pedido enviado", "Tu pedido está en camino", T1);

        assertThat(template.getCode()).isEqualTo("ORDER_SHIPPED");
        assertThat(template.isActive()).isTrue();
    }

    @Test
    void createConCodeVacioLanzaExcepcion() {
        assertThatThrownBy(() -> NotificationTemplate.create(
                NotificationTemplateId.generate(), "  ", "T", "B", T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateCambiaTituloYBody() {
        NotificationTemplate template = NotificationTemplate.create(
                NotificationTemplateId.generate(), "CODE", "Título", "Cuerpo", T1);

        template.update("Nuevo título", "Nuevo cuerpo", T1);

        assertThat(template.getTitle()).isEqualTo("Nuevo título");
        assertThat(template.getBody()).isEqualTo("Nuevo cuerpo");
    }

    @Test
    void activateYDeactivateCambianElEstado() {
        NotificationTemplate template = NotificationTemplate.create(
                NotificationTemplateId.generate(), "CODE", "Título", "Cuerpo", T1);

        template.deactivate();
        assertThat(template.isActive()).isFalse();

        template.activate();
        assertThat(template.isActive()).isTrue();
    }
}