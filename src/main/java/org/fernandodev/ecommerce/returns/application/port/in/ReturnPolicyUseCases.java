package org.fernandodev.ecommerce.returns.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;

/**
 * Casos de uso de políticas de devolución (puerto de entrada).
 */
public interface ReturnPolicyUseCases {

    /**
     * Crea una política de devoluciones.
     */
    ReturnPolicy create(CreateReturnPolicyCommand command);

    /**
     * Lista todas las políticas.
     */
    List<ReturnPolicy> list();

    /**
     * Consulta una política por identificador.
     */
    ReturnPolicy getById(ReturnPolicyId id);

    /**
     * Activa una política.
     */
    ReturnPolicy activate(ReturnPolicyId id);

    /**
     * Desactiva una política.
     */
    ReturnPolicy deactivate(ReturnPolicyId id);
}