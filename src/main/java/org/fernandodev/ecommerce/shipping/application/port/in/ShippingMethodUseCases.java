package org.fernandodev.ecommerce.shipping.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.shipping.application.dto.CreateShippingMethodCommand;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;

/**
 * Casos de uso de métodos de envío (puerto de entrada).
 */
public interface ShippingMethodUseCases {

    ShippingMethod create(CreateShippingMethodCommand command);

    List<ShippingMethod> list();

    ShippingMethod getById(ShippingMethodId id);

    ShippingMethod activate(ShippingMethodId id);

    ShippingMethod deactivate(ShippingMethodId id);
}