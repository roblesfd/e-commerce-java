package org.fernandodev.ecommerce.reviews.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.application.dto.CreateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.dto.RatingSummary;
import org.fernandodev.ecommerce.reviews.application.dto.UpdateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.reviews.application.port.out.OrderPort;
import org.fernandodev.ecommerce.reviews.application.port.out.ReviewRepository;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewReply;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de reviews.
 * <p>
 * Al crear un review se valida el rating, se previene el duplicado por
 * (cliente, producto) y se verifica la compra de forma best-effort: si el
 * cliente tiene una orden entregada con el producto, se marca como compra
 * verificada y se asocia el item de la orden.
 * </p>
 */
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCases {

    private final ReviewRepository reviews;
    private final OrderPort orders;

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review create(UUID customerId, ProductId productId, CreateReviewCommand command) {
        if (reviews.existsByCustomerAndProduct(customerId, productId)) {
            throw new ConflictException("El cliente ya ha reviewado este producto");
        }
        Review review = Review.create(ReviewId.generate(), customerId, productId, command.variantId(),
                command.rating(), command.title(), command.comment(), Instant.now());
        orders.verifyPurchase(customerId, productId)
                .ifPresent(purchase -> review.markVerifiedPurchase(
                        purchase.orderId(), purchase.orderItemId(), Instant.now()));
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review update(ReviewId id, UpdateReviewCommand command) {
        Review review = getExisting(id);
        review.update(command.title(), command.comment(), command.rating(), Instant.now());
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review delete(ReviewId id) {
        Review review = getExisting(id);
        review.delete(Instant.now());
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review approve(ReviewId id) {
        Review review = getExisting(id);
        review.approve(Instant.now());
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review reject(ReviewId id) {
        Review review = getExisting(id);
        review.reject(Instant.now());
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review hide(ReviewId id) {
        Review review = getExisting(id);
        review.hide(Instant.now());
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review restore(ReviewId id) {
        Review review = getExisting(id);
        review.restore(Instant.now());
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Review addReply(ReviewId id, UUID authorId, String comment) {
        Review review = getExisting(id);
        review.addReply(new ReviewReply(UUID.randomUUID(), authorId, comment, Instant.now()));
        return reviews.save(review);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Review getById(ReviewId id) {
        return getExisting(id);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Review> listByProduct(ProductId productId) {
        return reviews.findByProductId(productId).stream()
                .filter(review -> review.getStatus() == ReviewStatus.PUBLISHED)
                .toList();
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Review> listByCustomer(UUID customerId) {
        return reviews.findByCustomerId(customerId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Review> listForModeration() {
        return reviews.findByStatus(ReviewStatus.PENDING_MODERATION);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public RatingSummary ratingSummary(ProductId productId) {
        List<Integer> ratings = reviews.findPublishedRatingsByProductId(productId);
        if (ratings.isEmpty()) {
            return RatingSummary.empty();
        }
        Map<Integer, Long> distribution = new HashMap<>();
        ratings.forEach(rating -> distribution.merge(rating, 1L, Long::sum));
        BigDecimal sum = ratings.stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
        return RatingSummary.of(ratings.size(), average, distribution);
    }

    private Review getExisting(ReviewId id) {
        return reviews.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review no encontrado: " + id));
    }
}