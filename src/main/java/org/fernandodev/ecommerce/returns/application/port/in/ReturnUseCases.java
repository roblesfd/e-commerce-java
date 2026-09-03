package org.fernandodev.ecommerce.returns.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnCommand;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;

/**
 * Casos de uso de devoluciones (puerto de entrada).
 */
public interface ReturnUseCases {

    Return create(OrderId orderId, CreateReturnCommand command);

    Return getById(ReturnId id);

    List<Return> listByCustomer(UUID customerId);

    Optional<Return> getByOrder(OrderId orderId);

    List<Return> listAll();

    Return approve(ReturnId id);

    Return reject(ReturnId id);

    Return cancel(ReturnId id);

    Return registerReturnShipment(ReturnId id, String trackingNumber);

    Return receive(ReturnId id);

    Return inspect(ReturnId id, String condition, String notes, boolean passed);

    Return complete(ReturnId id);

    Return retryRefund(ReturnId id);
}