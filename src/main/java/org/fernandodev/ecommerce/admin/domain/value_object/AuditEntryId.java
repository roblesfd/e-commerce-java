package org.fernandodev.ecommerce.admin.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una entrada de auditoría.
 */
public record AuditEntryId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public AuditEntryId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static AuditEntryId generate() {
        return new AuditEntryId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link AuditEntryId} con el valor dado.
     */
    public static AuditEntryId of(UUID value) {
        return new AuditEntryId(value);
    }
}