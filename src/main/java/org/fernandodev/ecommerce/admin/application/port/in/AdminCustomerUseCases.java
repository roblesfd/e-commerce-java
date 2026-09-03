package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.domain.Customer;

/**
 * Orquestación administrativa de clientes.
 */
public interface AdminCustomerUseCases {

    List<Customer> listAllCustomers();

    Customer getCustomerByIamUserId(UUID iamUserId);
}