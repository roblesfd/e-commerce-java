package org.fernandodev.ecommerce.shipping.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;

/**
 * Puerto de salida de persistencia del agregado {@link ShippingMethod}.
 */
public interface ShippingMethodRepository {

    /**
     * Persiste el método de envío.
     */
    ShippingMethod save(ShippingMethod method);

    /**
     * Consulta un método de envío.
     */
    Optional<ShippingMethod> findById(ShippingMethodId id);

    /**
     * Lista todos los métodos de envío.
     */
    List<ShippingMethod> findAll();

    boolean existsById(ShippingMethodId id);
}