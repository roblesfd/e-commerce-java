package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link CouponJpaEntity}.
 */
public interface CouponSpringDataRepository extends JpaRepository<CouponJpaEntity, UUID> {

    Optional<CouponJpaEntity> findByCode(String code);

    boolean existsByCode(String code);
}