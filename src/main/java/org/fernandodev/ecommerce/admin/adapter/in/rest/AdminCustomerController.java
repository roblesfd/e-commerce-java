package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminCustomerUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de clientes.
 */
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerUseCases adminCustomer;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping
    public List<Customer> list() {
        return adminCustomer.listAllCustomers();
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{iamUserId}")
    /**
     * Get.
     * @param iamUserId iamUserId.
     */
    public Customer get(@PathVariable UUID iamUserId) {
        return adminCustomer.getCustomerByIamUserId(iamUserId);
    }
}