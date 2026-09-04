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

    /**
     * Crea un envío para una orden confirmada y solicita el tracking al proveedor.
     */
    Shipment create(OrderId orderId, CreateShipmentCommand command);

    /**
     * Consulta un envío por identificador.
     */
    Shipment getById(ShipmentId id);

    /**
     * Lista los envíos de un cliente.
     */
    List<Shipment> listByCustomer(UUID customerId);

    /**
     * Consulta el envío de una orden.
     */
    Optional<Shipment> getByOrder(OrderId orderId);

    Money calculateCost(ShippingMethodId methodId);

    /**
     * Marca el envío como preparado.
     */
    Shipment markPrepared(ShipmentId id);

    /**
     * Marca el envío como enviado (y la orden como enviada).
     */
    Shipment markShipped(ShipmentId id);

    /**
     * Marca el envío como en tránsito.
     */
    Shipment markInTransit(ShipmentId id);

    /**
     * Marca el envío como fuera para entrega.
     */
    Shipment markOutForDelivery(ShipmentId id);

    /**
     * Marca el envío como entregado (y la orden como entregada).
     */
    Shipment markDelivered(ShipmentId id);

    /**
     * Marca el envío como fallido.
     */
    Shipment handleFailure(ShipmentId id);

    /**
     * Cancela el envío (solo PENDING o PREPARING).
     */
    Shipment cancel(ShipmentId id);

    /**
     * Procesa un callback del proveedor de envíos.
     */
    Shipment processWebhook(String trackingNumber, String providerStatus);
}