package org.fernandodev.ecommerce.admin.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.AuditEntry;

/**
 * Puerto de salida de persistencia de la auditoría.
 */
public interface AuditRepository {

    /**
     * Persiste la entrada de auditoría.
     *
     * @param entry entrada a guardar.
     * @return la entrada persistida.
     */
    AuditEntry save(AuditEntry entry);

    /**
     * @return todas las entradas de auditoría.
     */
    List<AuditEntry> findAll();

    /**
     * @param action acción a filtrar.
     * @return las entradas con esa acción.
     */
    List<AuditEntry> findByAction(String action);

    /**
     * @param actorId identificador del actor.
     * @return las entradas ejecutadas por ese actor.
     */
    List<AuditEntry> findByActorId(UUID actorId);
}