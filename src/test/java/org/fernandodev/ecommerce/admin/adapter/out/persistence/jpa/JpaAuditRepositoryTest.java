package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaAuditRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private AuditRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaEntrada() {
        UUID actor = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        repository.save(AuditEntry.create(AuditEntryId.generate(), actor, "DELETE_PRODUCT",
                "product", target, "Producto eliminado", AT));

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findByAction("DELETE_PRODUCT")).hasSize(1);
        assertThat(repository.findByActorId(actor)).hasSize(1);
    }
}