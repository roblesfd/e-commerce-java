package org.fernandodev.ecommerce.inventory.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un inventario dentro del contexto Inventory.
 */
public record InventoryId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public InventoryId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static InventoryId generate() {
        return new InventoryId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link InventoryId} con el valor dado.
     */
    public static InventoryId of(UUID value) {
        return new InventoryId(value);
    }
}