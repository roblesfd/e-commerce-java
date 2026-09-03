package org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA del agregado {@link ReviewJpaEntity}.
 */
public interface ReviewSpringDataRepository extends JpaRepository<ReviewJpaEntity, UUID> {

    List<ReviewJpaEntity> findByProductId(UUID productId);

    List<ReviewJpaEntity> findByCustomerId(UUID customerId);

    List<ReviewJpaEntity> findByStatus(ReviewStatus status);

    boolean existsByCustomerIdAndProductId(UUID customerId, UUID productId);

    @Query("select r.rating from ReviewJpaEntity r where r.productId = :productId and r.status = :status")
    List<Integer> findRatingsByProductIdAndStatus(@Param("productId") UUID productId,
                                                  @Param("status") ReviewStatus status);
}