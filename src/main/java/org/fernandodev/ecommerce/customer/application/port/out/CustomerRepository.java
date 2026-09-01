package org.fernandodev.ecommerce.customer.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.CustomerId;

/**
 * Puerto de salida de persistencia del agregado {@link Customer}.
 */
public interface CustomerRepository {

    Optional<Customer> findById(CustomerId id);

    Optional<Customer> findByIamUserId(UUID iamUserId);

    boolean existsByEmail(String email);

    Customer save(Customer customer);
}