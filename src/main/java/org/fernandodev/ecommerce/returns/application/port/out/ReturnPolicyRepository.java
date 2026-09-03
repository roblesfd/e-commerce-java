package org.fernandodev.ecommerce.returns.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;

/**
 * Puerto de salida de persistencia del agregado {@link ReturnPolicy}.
 */
public interface ReturnPolicyRepository {

    ReturnPolicy save(ReturnPolicy policy);

    Optional<ReturnPolicy> findById(ReturnPolicyId id);

    Optional<ReturnPolicy> findActive();

    List<ReturnPolicy> findAll();
}