package org.fernandodev.ecommerce.shipping.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un método de envío.
 */
public record ShippingMethodId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ShippingMethodId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ShippingMethodId generate() {
        return new ShippingMethodId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ShippingMethodId} con el valor dado.
     */
    public static ShippingMethodId of(UUID value) {
        return new ShippingMethodId(value);
    }
}