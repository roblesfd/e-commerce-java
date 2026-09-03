package org.fernandodev.ecommerce.shipping.application.service;

import java.util.List;

import org.fernandodev.ecommerce.shipping.application.dto.CreateShippingMethodCommand;
import org.fernandodev.ecommerce.shipping.application.port.in.ShippingMethodUseCases;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de métodos de envío.
 */
@RequiredArgsConstructor
public class ShippingMethodService implements ShippingMethodUseCases {

    private final ShippingMethodRepository methods;

    @Override
    public ShippingMethod create(CreateShippingMethodCommand command) {
        ShippingMethod method = ShippingMethod.create(
                ShippingMethodId.generate(), command.name(), command.carrier(), command.cost());
        return methods.save(method);
    }

    @Override
    public List<ShippingMethod> list() {
        return methods.findAll();
    }

    @Override
    public ShippingMethod getById(ShippingMethodId id) {
        return getExisting(id);
    }

    @Override
    public ShippingMethod activate(ShippingMethodId id) {
        ShippingMethod method = getExisting(id);
        method.activate();
        return methods.save(method);
    }

    @Override
    public ShippingMethod deactivate(ShippingMethodId id) {
        ShippingMethod method = getExisting(id);
        method.deactivate();
        return methods.save(method);
    }

    private ShippingMethod getExisting(ShippingMethodId id) {
        return methods.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Método de envío no encontrado: " + id));
    }
}