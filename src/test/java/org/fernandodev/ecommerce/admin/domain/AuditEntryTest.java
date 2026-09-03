package org.fernandodev.ecommerce.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;
import org.junit.jupiter.api.Test;

class AuditEntryTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createRegistraLosDatosDeLaAccion() {
        UUID actor = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        AuditEntry entry = AuditEntry.create(AuditEntryId.generate(), actor, "DELETE_PRODUCT",
                "product", target, "Producto eliminado", T1);

        assertThat(entry.getActorId()).isEqualTo(actor);
        assertThat(entry.getAction()).isEqualTo("DELETE_PRODUCT");
        assertThat(entry.getTargetType()).isEqualTo("product");
        assertThat(entry.getTargetId()).isEqualTo(target);
    }
}