package org.fernandodev.ecommerce.customer.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;

/**
 * Puerto de salida de persistencia del agregado {@link Customer}.
 */
public interface CustomerRepository {

    /**
     * @param id identificador del cliente.
     * @return el cliente, o {@code Optional.empty()} si no existe.
     */
    Optional<Customer> findById(CustomerId id);

    /**
     * @param iamUserId identificador del usuario IAM.
     * @return el cliente vinculado a ese usuario, o {@code Optional.empty()} si no existe.
     */
    Optional<Customer> findByIamUserId(UUID iamUserId);

    /**
     * @param email correo electrónico.
     * @return {@code true} si ya existe un cliente con ese correo.
     */
    boolean existsByEmail(String email);

    /**
     * Persiste el cliente.
     *
     * @param customer cliente a guardar.
     * @return el cliente persistido.
     */
    Customer save(Customer customer);

    /**
     * @return todos los clientes.
     */
    List<Customer> findAll();
}
