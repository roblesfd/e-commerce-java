package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.admin.application.port.out.SystemConfigRepository;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaSystemConfigRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SystemConfigRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaConfiguracion() {
        repository.save(SystemConfig.create("order.returnWindowDays", "30", AT));

        assertThat(repository.findById("order.returnWindowDays")).isPresent();
        assertThat(repository.findById("order.returnWindowDays").orElseThrow().getValue()).isEqualTo("30");
        assertThat(repository.findAll()).hasSize(1);
    }
}