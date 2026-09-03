package org.fernandodev.ecommerce.shipping.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.application.dto.CreateShipmentCommand;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Casos de uso de envíos (puerto de entrada).
 */
public interface ShipmentUseCases {

    Shipment create(OrderId orderId, CreateShipmentCommand command);

    Shipment getById(ShipmentId id);

    List<Shipment> listByCustomer(UUID customerId);

    Optional<Shipment> getByOrder(OrderId orderId);

    Money calculateCost(ShippingMethodId methodId);

    Shipment markPrepared(ShipmentId id);

    Shipment markShipped(ShipmentId id);

    Shipment markInTransit(ShipmentId id);

    Shipment markOutForDelivery(ShipmentId id);

    Shipment markDelivered(ShipmentId id);

    Shipment handleFailure(ShipmentId id);

    Shipment cancel(ShipmentId id);

    Shipment processWebhook(String trackingNumber, String providerStatus);
}