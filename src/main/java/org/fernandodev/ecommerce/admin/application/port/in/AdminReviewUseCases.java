package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;

import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;

/**
 * Orquestación administrativa de moderación de reviews.
 */
public interface AdminReviewUseCases {

/**
     * Lista los reviews pendientes de moderación.
     */
    List<Review> listReviewsForModeration();

/**
     * Aprueba un review y lo registra en auditoría.
     */
    Review approveReview(ReviewId id);

/**
     * Rechaza un review y lo registra en auditoría.
     */
    Review rejectReview(ReviewId id);

/**
     * Oculta un review y lo registra en auditoría.
     */
    Review hideReview(ReviewId id);

/**
     * Restaura un review y lo registra en auditoría.
     */
    Review restoreReview(ReviewId id);
}