package org.fernandodev.ecommerce.admin.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.AuditEntry;

/**
 * Puerto de salida de persistencia de la auditoría.
 */
public interface AuditRepository {

    AuditEntry save(AuditEntry entry);

    List<AuditEntry> findAll();

    List<AuditEntry> findByAction(String action);

    List<AuditEntry> findByActorId(UUID actorId);
}