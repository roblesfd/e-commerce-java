package org.fernandodev.ecommerce.admin.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entrada de auditoría de una acción administrativa sensible.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEntry {

    private AuditEntryId id;
    private UUID actorId;
    private String action;
    private String targetType;
    private UUID targetId;
    private String detail;
    private Instant createdAt;

    /**
     * Crea una entrada de auditoría.
     *
     * @param id         identificador de la entrada.
     * @param actorId    identificador del usuario que ejecutó la acción.
     * @param action     acción ejecutada (p. ej. {@code DELETE_PRODUCT}).
     * @param targetType tipo de recurso afectado (p. ej. {@code product}).
     * @param targetId   identificador del recurso afectado (opcional).
     * @param detail     detalle de la acción.
     * @param at         momento de la acción.
     * @return la entrada creada.
     * @throws NullPointerException si {@code id}, {@code actorId}, {@code action} o {@code at} son {@code null}.
     */
    public static AuditEntry create(AuditEntryId id, UUID actorId, String action,
                                    String targetType, UUID targetId, String detail, Instant at) {
        AuditEntry entry = new AuditEntry();
        entry.id = Objects.requireNonNull(id, "id no puede ser null");
        entry.actorId = Objects.requireNonNull(actorId, "actorId no puede ser null");
        entry.action = Objects.requireNonNull(action, "action no puede ser null");
        entry.targetType = targetType;
        entry.targetId = targetId;
        entry.detail = detail;
        entry.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        return entry;
    }

    /**
     * Alias de {@link #create(AuditEntryId, UUID, String, String, UUID, String, Instant)} usado por la persistencia.
     *
     * @return la entrada de auditoría creada.
     */
    public static AuditEntry crear(AuditEntryId id, UUID actorId, String action,
                                   String targetType, UUID targetId, String detail, Instant createdAt) {
        return create(id, actorId, action, targetType, targetId, detail, createdAt);
    }
}