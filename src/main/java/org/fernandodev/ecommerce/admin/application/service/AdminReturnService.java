package org.fernandodev.ecommerce.admin.application.service;

import java.util.List;

import org.fernandodev.ecommerce.admin.application.port.in.AdminReturnUseCases;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de devoluciones.
 */
@RequiredArgsConstructor
public class AdminReturnService implements AdminReturnUseCases {

    private final ReturnUseCases returns;
    private final AuditService audit;

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Return> listAllReturns() {
        return returns.listAll();
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Return getReturn(ReturnId id) {
        return returns.getById(id);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return approveReturn(ReturnId id) {
        audit.record("APPROVE_RETURN", "return", id.value(), "Devolución aprobada");
        return returns.approve(id);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return rejectReturn(ReturnId id) {
        audit.record("REJECT_RETURN", "return", id.value(), "Devolución rechazada");
        return returns.reject(id);
    }
}