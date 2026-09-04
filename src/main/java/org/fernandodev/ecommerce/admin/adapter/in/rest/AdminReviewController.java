package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminReviewUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de moderación de reviews.
 */
@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewUseCases adminReview;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/moderation")
    public List<Review> pending() {
        return adminReview.listReviewsForModeration();
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/approve")
    /**
     * Approve.
     * @param id id.
     */
    public Review approve(@PathVariable UUID id) {
        return adminReview.approveReview(ReviewId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/reject")
    /**
     * Reject.
     * @param id id.
     */
    public Review reject(@PathVariable UUID id) {
        return adminReview.rejectReview(ReviewId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/hide")
    /**
     * Hide.
     * @param id id.
     */
    public Review hide(@PathVariable UUID id) {
        return adminReview.hideReview(ReviewId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/restore")
    /**
     * Restore.
     * @param id id.
     */
    public Review restore(@PathVariable UUID id) {
        return adminReview.restoreReview(ReviewId.of(id));
    }
}