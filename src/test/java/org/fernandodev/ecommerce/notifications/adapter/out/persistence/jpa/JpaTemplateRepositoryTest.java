package org.fernandodev.ecommerce.notifications.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.notifications.application.port.out.TemplateRepository;
import org.fernandodev.ecommerce.notifications.domain.NotificationTemplate;
import org.fernandodev.ecommerce.notifications.domain.value_object.NotificationTemplateId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaTemplateRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TemplateRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaPlantilla() {
        NotificationTemplate template = NotificationTemplate.create(
                NotificationTemplateId.generate(), "ORDER_SHIPPED", "Enviado", "En camino", AT);

        repository.save(template);
        NotificationTemplate loaded = repository.findById(template.getId()).orElseThrow();

        assertThat(loaded.getCode()).isEqualTo("ORDER_SHIPPED");
        assertThat(loaded.isActive()).isTrue();
    }

    @Test
    void findByCodeDevuelveLaPlantilla() {
        repository.save(NotificationTemplate.create(
                NotificationTemplateId.generate(), "ORDER_SHIPPED", "Enviado", "En camino", AT));

        assertThat(repository.findByCode("ORDER_SHIPPED")).isPresent();
    }

    @Test
    void findAllDevuelveTodas() {
        repository.save(NotificationTemplate.create(
                NotificationTemplateId.generate(), "A", "T", "B", AT));
        repository.save(NotificationTemplate.create(
                NotificationTemplateId.generate(), "B", "T", "B", AT));

        assertThat(repository.findAll()).hasSize(2);
    }
}