package org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de la entidad {@link CustomerJpaEntity}.
 */
public interface CustomerSpringDataRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByIamUserId(UUID iamUserId);

    boolean existsByEmail(String email);
}
