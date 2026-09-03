package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;

/**
 * Orquestación administrativa de devoluciones.
 */
public interface AdminReturnUseCases {

    List<Return> listAllReturns();

    Return getReturn(ReturnId id);

    Return approveReturn(ReturnId id);

    Return rejectReturn(ReturnId id);
}