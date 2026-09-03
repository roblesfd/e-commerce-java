package org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import org.fernandodev.ecommerce.reviews.adapter.util.mapper.ReviewPersistenceMapper;
import org.fernandodev.ecommerce.reviews.application.port.out.ReviewRepository;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link ReviewRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaReviewRepository implements ReviewRepository {

    private final ReviewSpringDataRepository springData;

    @Override
    public Review save(Review review) {
        ReviewJpaEntity saved = springData.save(ReviewPersistenceMapper.toEntity(review));
        return ReviewPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Review> findById(ReviewId id) {
        return springData.findById(id.value()).map(ReviewPersistenceMapper::toDomain);
    }

    @Override
    public List<Review> findByProductId(ProductId productId) {
        return springData.findByProductId(productId.value()).stream()
                .map(ReviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByCustomerId(UUID customerId) {
        return springData.findByCustomerId(customerId).stream()
                .map(ReviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByStatus(ReviewStatus status) {
        return springData.findByStatus(status).stream()
                .map(ReviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCustomerAndProduct(UUID customerId, ProductId productId) {
        return springData.existsByCustomerIdAndProductId(customerId, productId.value());
    }

    @Override
    public List<Integer> findPublishedRatingsByProductId(ProductId productId) {
        return springData.findRatingsByProductIdAndStatus(productId.value(), ReviewStatus.PUBLISHED);
    }
}