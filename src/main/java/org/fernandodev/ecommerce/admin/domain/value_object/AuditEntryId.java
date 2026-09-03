package org.fernandodev.ecommerce.admin.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una entrada de auditoría.
 */
public record AuditEntryId(UUID value) {

    public AuditEntryId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static AuditEntryId generate() {
        return new AuditEntryId(UUID.randomUUID());
    }

    public static AuditEntryId of(UUID value) {
        return new AuditEntryId(value);
    }
}