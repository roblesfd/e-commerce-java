package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminCustomerUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de clientes.
 * <p>
 * Permite consultar los clientes registrados. Todos los endpoints están
 * protegidos y requieren rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerUseCases adminCustomer;

    /**
     * Lista todos los clientes registrados.
     *
     * @return los clientes existentes.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping
    public List<Customer> list() {
        return adminCustomer.listAllCustomers();
    }

    /**
     * Consulta un cliente por su identificador IAM.
     *
     * @param iamUserId identificador IAM del cliente.
     * @return el cliente encontrado.
     * @throws ResourceNotFoundException si no existe un cliente con ese identificador.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{iamUserId}")
    public Customer get(@PathVariable UUID iamUserId) {
        return adminCustomer.getCustomerByIamUserId(iamUserId);
    }
}