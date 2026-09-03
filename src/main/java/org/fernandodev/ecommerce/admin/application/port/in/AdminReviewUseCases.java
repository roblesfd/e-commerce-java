package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;

/**
 * Orquestación administrativa de moderación de reviews.
 */
public interface AdminReviewUseCases {

    List<Review> listReviewsForModeration();

    Review approveReview(ReviewId id);

    Review rejectReview(ReviewId id);

    Review hideReview(ReviewId id);

    Review restoreReview(ReviewId id);
}