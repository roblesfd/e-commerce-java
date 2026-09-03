package org.fernandodev.ecommerce.reviews.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;

/**
 * Respuesta de un review.
 */
public record ReviewResponse(
        UUID id,
        UUID customerId,
        UUID productId,
        UUID variantId,
        UUID orderItemId,
        int rating,
        String title,
        String comment,
        ReviewStatus status,
        boolean verifiedPurchase,
        List<ReplyResponse> replies,
        Instant createdAt,
        Instant updatedAt
) {

    public record ReplyResponse(UUID replyId, UUID authorId, String comment, Instant at) {
    }
}