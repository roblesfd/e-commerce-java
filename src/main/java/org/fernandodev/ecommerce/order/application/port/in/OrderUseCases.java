package org.fernandodev.ecommerce.order.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Casos de uso del contexto Order Management (puerto de entrada).
 */
public interface OrderUseCases {

    /**
     * Crea una orden en estado {@code PENDING}.
     */
    Order create(CreateOrderCommand command);

    /**
     * Consulta una orden por identificador.
     */
    Order getById(OrderId id);

    /**
     * Lista las órdenes de un cliente.
     */
    List<Order> listByCustomer(UUID customerId);

    /**
     * Lista todas las órdenes (uso administrativo).
     */
    List<Order> listAll();

    /**
     * Confirma la orden.
     */
    Order confirm(OrderId id);

    /**
     * Procesa la orden.
     */
    Order process(OrderId id);

    /**
     * Marca la orden como lista para envío.
     */
    Order markReadyToShip(OrderId id);

    /**
     * Marca la orden como enviada.
     */
    Order markShipped(OrderId id);

    /**
     * Marca la orden como entregada.
     */
    Order markDelivered(OrderId id);

    /**
     * Cancela la orden.
     */
    Order cancel(OrderId id);
}