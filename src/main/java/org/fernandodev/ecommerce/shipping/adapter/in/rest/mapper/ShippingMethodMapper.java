package org.fernandodev.ecommerce.shipping.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShippingMethodRequest;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShippingMethodResponse;
import org.fernandodev.ecommerce.shipping.application.dto.CreateShippingMethodCommand;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio de métodos de
 * envío. Sin lógica de negocio.
 */
@Component
public class ShippingMethodMapper {

    public CreateShippingMethodCommand toCommand(ShippingMethodRequest request) {
        return new CreateShippingMethodCommand(
                request.name(),
                request.carrier(),
                Money.of(request.cost().amount(), request.cost().currency()));
    }

    public ShippingMethodResponse toResponse(ShippingMethod method) {
        return new ShippingMethodResponse(
                method.getId().value(),
                method.getName(),
                method.getCarrier(),
                new ShippingMethodResponse.MoneyResponse(method.getCost().amount(), method.getCost().currencyCode()),
                method.isActive());
    }
}