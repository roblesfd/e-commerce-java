package org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de la entidad {@link CustomerJpaEntity}.
 */
public interface CustomerSpringDataRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    /**
     * @param iamUserId identificador del usuario IAM.
     * @return la entidad del cliente vinculado, o {@code Optional.empty()} si no existe.
     */
    Optional<CustomerJpaEntity> findByIamUserId(UUID iamUserId);

    /**
     * @param email correo electrónico.
     * @return {@code true} si ya existe un cliente con ese correo.
     */
    boolean existsByEmail(String email);
}
