package org.fernandodev.ecommerce.shipping.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un método de envío.
 */
public record ShippingMethodId(UUID value) {

    public ShippingMethodId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ShippingMethodId generate() {
        return new ShippingMethodId(UUID.randomUUID());
    }

    public static ShippingMethodId of(UUID value) {
        return new ShippingMethodId(value);
    }
}