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

    /**
     * Persiste el envío.
     */
    Shipment save(Shipment shipment);

    /**
     * Consulta un envío por identificador.
     */
    Optional<Shipment> findById(ShipmentId id);

    /**
     * Consulta el envío de una orden.
     */
    Optional<Shipment> findByOrderId(OrderId orderId);

    /**
     * Consulta un envío por número de tracking.
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Lista los envíos de un cliente.
     */
    List<Shipment> findByCustomerId(UUID customerId);
}