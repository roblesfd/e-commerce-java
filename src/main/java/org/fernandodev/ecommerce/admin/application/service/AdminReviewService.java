package org.fernandodev.ecommerce.admin.application.service;

import java.util.List;

import org.fernandodev.ecommerce.admin.application.port.in.AdminReviewUseCases;
import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de moderación de reviews.
 */
@RequiredArgsConstructor
public class AdminReviewService implements AdminReviewUseCases {

    private final ReviewUseCases reviews;
    private final AuditService audit;

    @Override
    public List<Review> listReviewsForModeration() {
        return reviews.listForModeration();
    }

    @Override
    @Transactional
    public Review approveReview(ReviewId id) {
        audit.record("APPROVE_REVIEW", "review", id.value(), "Review aprobado");
        return reviews.approve(id);
    }

    @Override
    @Transactional
    public Review rejectReview(ReviewId id) {
        audit.record("REJECT_REVIEW", "review", id.value(), "Review rechazado");
        return reviews.reject(id);
    }

    @Override
    @Transactional
    public Review hideReview(ReviewId id) {
        audit.record("HIDE_REVIEW", "review", id.value(), "Review ocultado");
        return reviews.hide(id);
    }

    @Override
    @Transactional
    public Review restoreReview(ReviewId id) {
        audit.record("RESTORE_REVIEW", "review", id.value(), "Review restaurado");
        return reviews.restore(id);
    }
}