package org.fernandodev.ecommerce.reviews.adapter.util.mapper;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.ReviewReplyEmbeddable;
import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.ReviewStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewReply;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatusEntry;

/**
 * Mapeo entre el agregado {@link Review} y su entidad JPA.
 */
public final class ReviewPersistenceMapper {

    private ReviewPersistenceMapper() {
    }

    public static ReviewJpaEntity toEntity(Review review) {
        ReviewJpaEntity entity = new ReviewJpaEntity();
        entity.setId(review.getId().value());
        entity.setCustomerId(review.getCustomerId());
        entity.setProductId(review.getProductId().value());
        entity.setVariantId(review.getVariantId() == null ? null : review.getVariantId().value());
        entity.setOrderId(review.getOrderId() == null ? null : review.getOrderId().value());
        entity.setOrderItemId(review.getOrderItemId());
        entity.setRating(review.getRating());
        entity.setTitle(review.getTitle());
        entity.setComment(review.getComment());
        entity.setStatus(review.getStatus());
        entity.setVerifiedPurchase(review.isVerifiedPurchase());
        entity.setCreatedAt(review.getCreatedAt());
        entity.setStatusHistory(review.getStatusHistory().stream()
                .map(ReviewPersistenceMapper::toHistoryEmbeddable).toList());
        entity.setReplies(review.getReplies().stream()
                .map(ReviewPersistenceMapper::toReplyEmbeddable).toList());
        return entity;
    }

    public static Review toDomain(ReviewJpaEntity entity) {
        List<ReviewStatusEntry> history = entity.getStatusHistory().stream()
                .map(ReviewPersistenceMapper::toStatusEntry)
                .toList();
        List<ReviewReply> replies = entity.getReplies().stream()
                .map(ReviewPersistenceMapper::toReviewReply)
                .toList();
        return Review.crear(
                ReviewId.of(entity.getId()),
                entity.getCustomerId(),
                ProductId.of(entity.getProductId()),
                entity.getVariantId() == null ? null : ProductVariantId.of(entity.getVariantId()),
                entity.getOrderId() == null ? null : OrderId.of(entity.getOrderId()),
                entity.getOrderItemId(),
                entity.getRating(),
                entity.getTitle(),
                entity.getComment(),
                entity.getStatus(),
                entity.isVerifiedPurchase(),
                replies,
                history,
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }

    private static ReviewStatusHistoryEmbeddable toHistoryEmbeddable(ReviewStatusEntry entry) {
        ReviewStatusHistoryEmbeddable embeddable = new ReviewStatusHistoryEmbeddable();
        embeddable.setStatus(entry.status());
        embeddable.setAt(entry.at());
        return embeddable;
    }

    private static ReviewStatusEntry toStatusEntry(ReviewStatusHistoryEmbeddable embeddable) {
        return new ReviewStatusEntry(embeddable.getStatus(), embeddable.getAt());
    }

    private static ReviewReplyEmbeddable toReplyEmbeddable(ReviewReply reply) {
        ReviewReplyEmbeddable embeddable = new ReviewReplyEmbeddable();
        embeddable.setReplyId(reply.replyId());
        embeddable.setAuthorId(reply.authorId());
        embeddable.setComment(reply.comment());
        embeddable.setAt(reply.at());
        return embeddable;
    }

    private static ReviewReply toReviewReply(ReviewReplyEmbeddable embeddable) {
        return new ReviewReply(embeddable.getReplyId(), embeddable.getAuthorId(),
                embeddable.getComment(), embeddable.getAt());
    }
}