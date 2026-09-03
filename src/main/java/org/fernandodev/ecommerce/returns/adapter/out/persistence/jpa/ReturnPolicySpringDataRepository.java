package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity.ReturnPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link ReturnPolicyJpaEntity}.
 */
public interface ReturnPolicySpringDataRepository extends JpaRepository<ReturnPolicyJpaEntity, UUID> {

    Optional<ReturnPolicyJpaEntity> findFirstByActiveTrue();
}