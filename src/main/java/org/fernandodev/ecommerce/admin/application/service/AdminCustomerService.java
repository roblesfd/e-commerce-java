package org.fernandodev.ecommerce.admin.application.service;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminCustomerUseCases;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.customer.domain.Customer;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de clientes (solo lectura).
 */
@RequiredArgsConstructor
public class AdminCustomerService implements AdminCustomerUseCases {

    private final CustomerUseCases customers;

    @Override
    public List<Customer> listAllCustomers() {
        return customers.listAll();
    }

    @Override
    public Customer getCustomerByIamUserId(UUID iamUserId) {
        return customers.getByIamUserId(iamUserId);
    }
}