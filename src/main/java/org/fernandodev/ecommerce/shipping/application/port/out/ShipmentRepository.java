package org.fernandodev.ecommerce.shipping.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;

/**
 * Puerto de salida de persistencia del agregado {@link Shipment}.
 */
public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(ShipmentId id);

    Optional<Shipment> findByOrderId(OrderId orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByCustomerId(UUID customerId);
}