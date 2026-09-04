package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;

/**
 * Orquestación administrativa de devoluciones.
 */
public interface AdminReturnUseCases {

/**
     * Lista todas las devoluciones.
     */
    List<Return> listAllReturns();

/**
     * Consulta una devolución.
     */
    Return getReturn(ReturnId id);

/**
     * Aprueba una devolución y lo registra en auditoría.
     */
    Return approveReturn(ReturnId id);

/**
     * Rechaza una devolución y lo registra en auditoría.
     */
    Return rejectReturn(ReturnId id);
}