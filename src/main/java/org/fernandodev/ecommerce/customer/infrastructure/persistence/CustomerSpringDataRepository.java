package org.fernandodev.ecommerce.customer.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de la entidad {@link CustomerJpaEntity}.
 */
public interface CustomerSpringDataRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByIamUserId(UUID iamUserId);

    boolean existsByEmail(String email);
}