package org.fernandodev.ecommerce.returns.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;

/**
 * Puerto de salida de persistencia del agregado {@link Return}.
 */
public interface ReturnRepository {

    Return save(Return aReturn);

    Optional<Return> findById(ReturnId id);

    Optional<Return> findByOrderId(OrderId orderId);

    List<Return> findByCustomerId(UUID customerId);

    boolean existsByOrderId(OrderId orderId);

    List<Return> findAll();
}