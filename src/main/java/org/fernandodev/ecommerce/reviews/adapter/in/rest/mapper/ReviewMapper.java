package org.fernandodev.ecommerce.reviews.adapter.in.rest.mapper;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.CreateReviewRequest;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.ReviewResponse;
import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.UpdateReviewRequest;
import org.fernandodev.ecommerce.reviews.application.dto.CreateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.dto.UpdateReviewCommand;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link Review} y sus DTOs REST.
 */
@Component
public class ReviewMapper {

    public CreateReviewCommand toCreateCommand(CreateReviewRequest request) {
        return new CreateReviewCommand(request.rating(), request.title(), request.comment(),
                request.variantId() == null ? null : ProductVariantId.of(request.variantId()));
    }

    public UpdateReviewCommand toUpdateCommand(UpdateReviewRequest request) {
        return new UpdateReviewCommand(request.rating(), request.title(), request.comment());
    }

    public ReviewResponse toResponse(Review review) {
        List<ReviewResponse.ReplyResponse> replies = review.getReplies().stream()
                .map(reply -> new ReviewResponse.ReplyResponse(
                        reply.replyId(), reply.authorId(), reply.comment(), reply.at()))
                .toList();
        return new ReviewResponse(
                review.getId().value(),
                review.getCustomerId(),
                review.getProductId().value(),
                review.getVariantId() == null ? null : review.getVariantId().value(),
                review.getOrderItemId(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getStatus(),
                review.isVerifiedPurchase(),
                replies,
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}