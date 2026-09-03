package org.fernandodev.ecommerce.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class SystemConfigTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createGuardaClaveYValor() {
        SystemConfig config = SystemConfig.create("order.returnWindowDays", "30", T1);

        assertThat(config.getKey()).isEqualTo("order.returnWindowDays");
        assertThat(config.getValue()).isEqualTo("30");
    }

    @Test
    void createConClaveVaciaLanzaExcepcion() {
        assertThatThrownBy(() -> SystemConfig.create("  ", "30", T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateCambiaElValor() {
        SystemConfig config = SystemConfig.create("order.returnWindowDays", "30", T1);

        config.update("15", T1);

        assertThat(config.getValue()).isEqualTo("15");
    }
}