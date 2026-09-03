package org.fernandodev.ecommerce.shipping.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;

/**
 * Puerto de salida de persistencia del agregado {@link ShippingMethod}.
 */
public interface ShippingMethodRepository {

    ShippingMethod save(ShippingMethod method);

    Optional<ShippingMethod> findById(ShippingMethodId id);

    List<ShippingMethod> findAll();

    boolean existsById(ShippingMethodId id);
}