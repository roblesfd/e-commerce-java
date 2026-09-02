package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity.PromotionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/**
 * Repositorio Spring Data JPA del agregado {@link PromotionJpaEntity}.
 */
public interface PromotionSpringDataRepository extends JpaRepository<PromotionJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PromotionJpaEntity p where p.id = :id")
    Optional<PromotionJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}