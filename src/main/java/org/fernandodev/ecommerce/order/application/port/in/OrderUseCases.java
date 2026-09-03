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

    Order create(CreateOrderCommand command);

    Order getById(OrderId id);

    List<Order> listByCustomer(UUID customerId);

    List<Order> listAll();

    Order confirm(OrderId id);

    Order process(OrderId id);

    Order markReadyToShip(OrderId id);

    Order markShipped(OrderId id);

    Order markDelivered(OrderId id);

    Order cancel(OrderId id);
}