package org.fernandodev.ecommerce.admin.application.service;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;
import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.application.port.out.CurrentActorPort;

/**
 * Registra acciones administrativas sensibles en el trail de auditoría.
 */
public class AuditService {

    private final AuditRepository audit;
    private final CurrentActorPort actor;

    public AuditService(AuditRepository audit, CurrentActorPort actor) {
        this.audit = audit;
        this.actor = actor;
    }

    /**
     * Registra una acción administrativa en el trail de auditoría con el actor actual.
     *
     * @param action     acción ejecutada.
     * @param targetType tipo de recurso afectado.
     * @param targetId   identificador del recurso afectado (opcional).
     * @param detail     detalle de la acción.
     */
    public void record(String action, String targetType, UUID targetId, String detail) {
        audit.save(AuditEntry.create(AuditEntryId.generate(), actor.actorId(), action,
                targetType, targetId, detail, Instant.now()));
    }
}