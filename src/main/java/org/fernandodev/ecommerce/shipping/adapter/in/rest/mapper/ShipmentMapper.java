package org.fernandodev.ecommerce.shipping.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.CreateShipmentRequest;
import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShipmentResponse;
import org.fernandodev.ecommerce.shipping.application.dto.CreateShipmentCommand;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatusEntry;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio de envíos.
 */
@Component
public class ShipmentMapper {

    public CreateShipmentCommand toCommand(CreateShipmentRequest request) {
        return new CreateShipmentCommand(ShippingMethodId.of(request.shippingMethodId()), request.addressId());
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId().value(),
                shipment.getOrderId().value(),
                shipment.getCustomerId(),
                shipment.getShippingMethodId().value(),
                shipment.getAddressId(),
                shipment.getAddressSnapshot(),
                shipment.getTrackingNumber(),
                toMoney(shipment.getCost()),
                shipment.getStatus().name(),
                shipment.getStatusHistory().stream().map(this::toStatusEntry).toList(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt());
    }

    private ShipmentResponse.StatusEntryResponse toStatusEntry(ShipmentStatusEntry entry) {
        return new ShipmentResponse.StatusEntryResponse(entry.status().name(), entry.at());
    }

    private static ShipmentResponse.MoneyResponse toMoney(Money money) {
        return new ShipmentResponse.MoneyResponse(money.amount(), money.currencyCode());
    }

    public static OrderId order(CreateShipmentRequest request) {
        return OrderId.of(request.orderId());
    }
}