package org.fernandodev.ecommerce.shipping.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.shipping.application.dto.CreateShippingMethodCommand;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;

/**
 * Casos de uso de métodos de envío (puerto de entrada).
 */
public interface ShippingMethodUseCases {

    /**
     * Crea un método de envío.
     */
    ShippingMethod create(CreateShippingMethodCommand command);

    /**
     * Lista todos los métodos de envío.
     */
    List<ShippingMethod> list();

    /**
     * Consulta un método de envío.
     */
    ShippingMethod getById(ShippingMethodId id);

    /**
     * Activa un método de envío.
     */
    ShippingMethod activate(ShippingMethodId id);

    /**
     * Desactiva un método de envío.
     */
    ShippingMethod deactivate(ShippingMethodId id);
}