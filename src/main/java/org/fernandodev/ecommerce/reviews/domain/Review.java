package org.fernandodev.ecommerce.reviews.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewReply;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatusEntry;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa un review de un producto.
 * <p>
 * Los reviews nuevos entran en PENDING_MODERATION y se publican tras la
 * aprobación. Cuando la compra está verificada, el review referencia el
 * item de la orden que lo originó.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    private ReviewId id;
    private UUID customerId;
    private ProductId productId;
    private ProductVariantId variantId;
    private OrderId orderId;
    private UUID orderItemId;
    private int rating;
    private String title;
    private String comment;
    private ReviewStatus status;
    private boolean verifiedPurchase;
    @Getter(AccessLevel.NONE)
    private List<ReviewReply> replies;
    @Getter(AccessLevel.NONE)
    private List<ReviewStatusEntry> statusHistory;
    private Instant createdAt;
    private Instant updatedAt;

    public static Review create(ReviewId id, UUID customerId, ProductId productId, ProductVariantId variantId,
                                int rating, String title, String comment, Instant at) {
        Review review = new Review();
        review.id = Objects.requireNonNull(id, "id no puede ser null");
        review.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        review.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        review.variantId = variantId;
        review.orderId = null;
        review.orderItemId = null;
        review.rating = validateRating(rating);
        review.title = title;
        review.comment = Objects.requireNonNull(comment, "comment no puede ser null");
        review.status = ReviewStatus.PENDING_MODERATION;
        review.verifiedPurchase = false;
        review.replies = new ArrayList<>();
        review.statusHistory = new ArrayList<>(List.of(new ReviewStatusEntry(ReviewStatus.PENDING_MODERATION, at)));
        review.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        review.updatedAt = at;
        return review;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Review crear(
            ReviewId id,
            UUID customerId,
            ProductId productId,
            ProductVariantId variantId,
            OrderId orderId,
            UUID orderItemId,
            int rating,
            String title,
            String comment,
            ReviewStatus status,
            boolean verifiedPurchase,
            List<ReviewReply> replies,
            List<ReviewStatusEntry> statusHistory,
            Instant createdAt,
            Instant updatedAt
    ) {
        Review review = new Review();
        review.id = Objects.requireNonNull(id, "id no puede ser null");
        review.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        review.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        review.variantId = variantId;
        review.orderId = orderId;
        review.orderItemId = orderItemId;
        review.rating = validateRating(rating);
        review.title = title;
        review.comment = Objects.requireNonNull(comment, "comment no puede ser null");
        review.status = Objects.requireNonNull(status, "status no puede ser null");
        review.verifiedPurchase = verifiedPurchase;
        review.replies = new ArrayList<>(replies == null ? List.of() : replies);
        review.statusHistory = new ArrayList<>(statusHistory == null ? List.of() : statusHistory);
        review.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        review.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return review;
    }

    public List<ReviewReply> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public List<ReviewStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public void markVerifiedPurchase(OrderId orderId, UUID orderItemId, Instant at) {
        if (this.verifiedPurchase) {
            return;
        }
        this.verifiedPurchase = true;
        this.orderId = Objects.requireNonNull(orderId, "orderId no puede ser null");
        this.orderItemId = Objects.requireNonNull(orderItemId, "orderItemId no puede ser null");
        this.updatedAt = at;
    }

    public void update(String title, String comment, int rating, Instant at) {
        if (status != ReviewStatus.PENDING_MODERATION && status != ReviewStatus.PUBLISHED) {
            throw new BusinessRuleViolationException(
                    "Solo se puede actualizar un review PENDING_MODERATION o PUBLISHED");
        }
        this.title = title;
        this.comment = Objects.requireNonNull(comment, "comment no puede ser null");
        this.rating = validateRating(rating);
        this.updatedAt = at;
        if (status == ReviewStatus.PUBLISHED) {
            transition(ReviewStatus.PENDING_MODERATION, at);
        }
    }

    public void approve(Instant at) {
        requireTransition(ReviewStatus.PUBLISHED, "aprobar", at);
    }

    public void reject(Instant at) {
        requireTransition(ReviewStatus.REJECTED, "rechazar", at);
    }

    public void hide(Instant at) {
        requireTransition(ReviewStatus.HIDDEN, "ocultar", at);
    }

    public void restore(Instant at) {
        requireTransition(ReviewStatus.PUBLISHED, "restaurar", at);
    }

    public void delete(Instant at) {
        if (status == ReviewStatus.DELETED) {
            throw new BusinessRuleViolationException("El review ya está eliminado");
        }
        transition(ReviewStatus.DELETED, at);
    }

    public void addReply(ReviewReply reply) {
        if (status != ReviewStatus.PUBLISHED) {
            throw new BusinessRuleViolationException("Solo se puede responder a un review publicado");
        }
        replies.add(Objects.requireNonNull(reply, "reply no puede ser null"));
        this.updatedAt = reply.at();
    }

    public boolean isTerminal() {
        return status == ReviewStatus.REJECTED || status == ReviewStatus.DELETED;
    }

    private void requireTransition(ReviewStatus target, String operacion, Instant at) {
        boolean valid = switch (target) {
            case PUBLISHED -> status == ReviewStatus.PENDING_MODERATION || status == ReviewStatus.HIDDEN;
            case REJECTED -> status == ReviewStatus.PENDING_MODERATION;
            case HIDDEN -> status == ReviewStatus.PUBLISHED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessRuleViolationException(
                    "No se puede " + operacion + " un review en estado " + status);
        }
        transition(target, at);
    }

    private void transition(ReviewStatus target, Instant at) {
        this.status = target;
        this.updatedAt = at;
        statusHistory.add(new ReviewStatusEntry(target, at));
    }

    private static int validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessRuleViolationException("El rating debe estar entre 1 y 5");
        }
        return rating;
    }
}