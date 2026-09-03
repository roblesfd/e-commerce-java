package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import java.util.UUID;

import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity.ShippingMethodJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link ShippingMethodJpaEntity}.
 */
public interface ShippingMethodSpringDataRepository extends JpaRepository<ShippingMethodJpaEntity, UUID> {
}