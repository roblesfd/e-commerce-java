package org.fernandodev.ecommerce.reviews.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;

/**
 * Puerto de salida de persistencia del agregado {@link Review}.
 */
public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(ReviewId id);

    List<Review> findByProductId(ProductId productId);

    List<Review> findByCustomerId(UUID customerId);

    List<Review> findByStatus(ReviewStatus status);

    boolean existsByCustomerAndProduct(UUID customerId, ProductId productId);

    List<Integer> findPublishedRatingsByProductId(ProductId productId);
}