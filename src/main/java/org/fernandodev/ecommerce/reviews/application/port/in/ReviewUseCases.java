package org.fernandodev.ecommerce.reviews.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.application.dto.CreateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.dto.RatingSummary;
import org.fernandodev.ecommerce.reviews.application.dto.UpdateReviewCommand;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;

/**
 * Casos de uso de reviews (puerto de entrada).
 */
public interface ReviewUseCases {

    Review create(UUID customerId, ProductId productId, CreateReviewCommand command);

    Review update(ReviewId id, UpdateReviewCommand command);

    Review delete(ReviewId id);

    Review approve(ReviewId id);

    Review reject(ReviewId id);

    Review hide(ReviewId id);

    Review restore(ReviewId id);

    Review addReply(ReviewId id, UUID authorId, String comment);

    Review getById(ReviewId id);

    List<Review> listByProduct(ProductId productId);

    List<Review> listByCustomer(UUID customerId);

    List<Review> listForModeration();

    RatingSummary ratingSummary(ProductId productId);
}