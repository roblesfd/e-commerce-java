package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminReturnUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de devoluciones.
 * <p>
 * Permite listar las devoluciones y decidir su aprobación o rechazo.
 * Todos los endpoints están protegidos y requieren rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController {

    private final AdminReturnUseCases adminReturn;

    /**
     * Lista todas las devoluciones registradas.
     *
     * @return las devoluciones existentes.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping
    public List<Return> list() {
        return adminReturn.listAllReturns();
    }

    /**
     * Consulta una devolución por identificador.
     *
     * @param id identificador de la devolución.
     * @return la devolución encontrada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{id}")
    public Return get(@PathVariable UUID id) {
        return adminReturn.getReturn(ReturnId.of(id));
    }

    /**
     * Aprueba una devolución, habilitando su reembolso.
     *
     * @param id identificador de la devolución.
     * @return la devolución aprobada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/approve")
    public Return approve(@PathVariable UUID id) {
        return adminReturn.approveReturn(ReturnId.of(id));
    }

    /**
     * Rechaza una devolución.
     *
     * @param id identificador de la devolución.
     * @return la devolución rechazada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/reject")
    public Return reject(@PathVariable UUID id) {
        return adminReturn.rejectReturn(ReturnId.of(id));
    }
}