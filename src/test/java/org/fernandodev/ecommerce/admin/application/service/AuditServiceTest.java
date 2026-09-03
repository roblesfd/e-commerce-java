package org.fernandodev.ecommerce.admin.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.application.port.out.CurrentActorPort;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;
import org.junit.jupiter.api.Test;

class AuditServiceTest {

    private final FakeAuditRepository repository = new FakeAuditRepository();
    private final UUID actor = UUID.randomUUID();
    private final AuditService service = new AuditService(repository, () -> actor);

    @Test
    void recordGuardaLaEntradaConElActorActual() {
        UUID target = UUID.randomUUID();

        service.record("DELETE_PRODUCT", "product", target, "Producto eliminado");

        assertThat(repository.findAll()).hasSize(1);
        AuditEntry entry = repository.findAll().get(0);
        assertThat(entry.getActorId()).isEqualTo(actor);
        assertThat(entry.getAction()).isEqualTo("DELETE_PRODUCT");
        assertThat(entry.getTargetId()).isEqualTo(target);
    }

    static final class FakeAuditRepository implements AuditRepository {

        private final Map<org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId, AuditEntry> byId =
                new HashMap<>();

        @Override
        public AuditEntry save(AuditEntry entry) {
            byId.put(entry.getId(), entry);
            return entry;
        }

        @Override
        public java.util.List<AuditEntry> findAll() {
            return java.util.List.copyOf(byId.values());
        }

        @Override
        public java.util.List<AuditEntry> findByAction(String action) {
            return byId.values().stream().filter(e -> e.getAction().equals(action)).toList();
        }

        @Override
        public java.util.List<AuditEntry> findByActorId(UUID actorId) {
            return byId.values().stream().filter(e -> e.getActorId().equals(actorId)).toList();
        }
    }
}