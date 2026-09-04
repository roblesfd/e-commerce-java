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
 */
@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController {

    private final AdminReturnUseCases adminReturn;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping
    public List<Return> list() {
        return adminReturn.listAllReturns();
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public Return get(@PathVariable UUID id) {
        return adminReturn.getReturn(ReturnId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/approve")
    /**
     * Approve.
     * @param id id.
     */
    public Return approve(@PathVariable UUID id) {
        return adminReturn.approveReturn(ReturnId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/reject")
    /**
     * Reject.
     * @param id id.
     */
    public Return reject(@PathVariable UUID id) {
        return adminReturn.rejectReturn(ReturnId.of(id));
    }
}