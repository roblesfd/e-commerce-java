package org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.fernandodev.ecommerce.customer.adapter.util.mapper.CustomerPersistenceMapper;
import org.fernandodev.ecommerce.customer.application.port.out.CustomerRepository;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.customer.domain.value_object.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa {@link CustomerRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaCustomerRepository implements CustomerRepository {

    private final CustomerSpringDataRepository springData;

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return springData.findById(id.value()).map(CustomerPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByIamUserId(UUID iamUserId) {
        return springData.findByIamUserId(iamUserId).map(CustomerPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springData.existsByEmail(email);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity saved = springData.save(CustomerPersistenceMapper.toEntity(customer));
        return CustomerPersistenceMapper.toDomain(saved);
    }
}
