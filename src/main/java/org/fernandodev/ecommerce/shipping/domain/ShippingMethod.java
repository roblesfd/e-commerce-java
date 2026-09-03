package org.fernandodev.ecommerce.shipping.domain;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa un método de envío (con carrier y costo).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingMethod {

    private ShippingMethodId id;
    private String name;
    private String carrier;
    private Money cost;
    private boolean active;

    public static ShippingMethod create(ShippingMethodId id, String name, String carrier, Money cost) {
        return crear(id, name, carrier, cost, true);
    }

    public static ShippingMethod crear(ShippingMethodId id, String name, String carrier, Money cost, boolean active) {
        ShippingMethod method = new ShippingMethod();
        method.id = Objects.requireNonNull(id, "id no puede ser null");
        method.name = requireText(name, "name");
        method.carrier = requireText(carrier, "carrier");
        method.cost = Objects.requireNonNull(cost, "cost no puede ser null");
        if (cost.isNegativeOrZero() && !cost.isZero()) {
            throw new BusinessRuleViolationException("El costo del envío no puede ser negativo");
        }
        method.active = active;
        return method;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException(name + " es obligatorio");
        }
        return value.trim();
    }
}