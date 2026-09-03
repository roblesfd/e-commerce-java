package org.fernandodev.ecommerce.returns.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;

/**
 * Casos de uso de políticas de devolución (puerto de entrada).
 */
public interface ReturnPolicyUseCases {

    ReturnPolicy create(CreateReturnPolicyCommand command);

    List<ReturnPolicy> list();

    ReturnPolicy getById(ReturnPolicyId id);

    ReturnPolicy activate(ReturnPolicyId id);

    ReturnPolicy deactivate(ReturnPolicyId id);
}