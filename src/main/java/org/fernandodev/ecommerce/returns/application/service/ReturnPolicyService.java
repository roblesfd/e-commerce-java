package org.fernandodev.ecommerce.returns.application.service;

import java.util.List;

import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnPolicyUseCases;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de políticas de devolución.
 */
@RequiredArgsConstructor
public class ReturnPolicyService implements ReturnPolicyUseCases {

    private final ReturnPolicyRepository policies;

    /**
     * {@inheritDoc}
     */
    @Override
    public ReturnPolicy create(CreateReturnPolicyCommand command) {
        ReturnPolicy policy = ReturnPolicy.create(
                ReturnPolicyId.generate(), command.returnWindowDays(), command.returnableProductIds());
        return policies.save(policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReturnPolicy> list() {
        return policies.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReturnPolicy getById(ReturnPolicyId id) {
        return getExisting(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReturnPolicy activate(ReturnPolicyId id) {
        ReturnPolicy policy = getExisting(id);
        policy.activate();
        return policies.save(policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReturnPolicy deactivate(ReturnPolicyId id) {
        ReturnPolicy policy = getExisting(id);
        policy.deactivate();
        return policies.save(policy);
    }

    private ReturnPolicy getExisting(ReturnPolicyId id) {
        return policies.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Política de devoluciones no encontrada: " + id));
    }
}
