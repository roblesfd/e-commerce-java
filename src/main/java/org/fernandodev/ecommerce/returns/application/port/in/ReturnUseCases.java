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

    /**
     * Crea una devolución validando elegibilidad (orden entregada, ventana, cantidades).
     */
    Return create(OrderId orderId, CreateReturnCommand command);

    /**
     * Consulta una devolución por identificador.
     */
    Return getById(ReturnId id);

    /**
     * Lista las devoluciones de un cliente.
     */
    List<Return> listByCustomer(UUID customerId);

    /**
     * Consulta la devolución de una orden.
     */
    Optional<Return> getByOrder(OrderId orderId);

    /**
     * Lista todas las devoluciones (uso administrativo).
     */
    List<Return> listAll();

    /**
     * Aprueba la devolución y calcula el reembolso.
     */
    Return approve(ReturnId id);

    /**
     * Rechaza la devolución.
     */
    Return reject(ReturnId id);

    /**
     * Cancela la devolución.
     */
    Return cancel(ReturnId id);

    /**
     * Registra el envío de devolución (IN_TRANSIT).
     */
    Return registerReturnShipment(ReturnId id, String trackingNumber);

    /**
     * Marca la devolución como recibida.
     */
    Return receive(ReturnId id);

    /**
     * Registra el resultado de la inspección.
     */
    Return inspect(ReturnId id, String condition, String notes, boolean passed);

    /**
     * Completa la devolución y solicita el reembolso al contexto Payment.
     */
    Return complete(ReturnId id);

    /**
     * Reintenta el reembolso de una devolución fallida.
     */
    Return retryRefund(ReturnId id);
}