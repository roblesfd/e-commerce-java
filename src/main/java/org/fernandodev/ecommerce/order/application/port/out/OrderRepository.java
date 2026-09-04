package org.fernandodev.ecommerce.order.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;

/**
 * Puerto de salida de persistencia del agregado {@link Order}.
 */
public interface OrderRepository {

    /**
     * Persiste la orden.
     */
    Order save(Order order);

    /**
     * Consulta una orden por identificador.
     */
    Optional<Order> findById(OrderId id);

    /**
     * Lista las órdenes de un cliente.
     */
    List<Order> findByCustomerId(UUID customerId);

    /**
     * Lista todas las órdenes.
     */
    List<Order> findAll();
}