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
 * <p>
 * Permite listar las reviews pendientes de moderación y aprobarlas, rechazarlas,
 * ocultarlas o restaurarlas. Todos los endpoints están protegidos y requieren
 * rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewUseCases adminReview;

    /**
     * Lista las reviews pendientes de moderación.
     *
     * @return las reviews pendientes.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/moderation")
    public List<Review> pending() {
        return adminReview.listReviewsForModeration();
    }

    /**
     * Aprueba una review pendiente.
     *
     * @param id identificador de la review.
     * @return la review aprobada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/approve")
    public Review approve(@PathVariable UUID id) {
        return adminReview.approveReview(ReviewId.of(id));
    }

    /**
     * Rechaza una review pendiente.
     *
     * @param id identificador de la review.
     * @return la review rechazada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/reject")
    public Review reject(@PathVariable UUID id) {
        return adminReview.rejectReview(ReviewId.of(id));
    }

    /**
     * Oculta una review publicada.
     *
     * @param id identificador de la review.
     * @return la review oculta.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/hide")
    public Review hide(@PathVariable UUID id) {
        return adminReview.hideReview(ReviewId.of(id));
    }

    /**
     * Restaura una review oculta.
     *
     * @param id identificador de la review.
     * @return la review restaurada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/restore")
    public Review restore(@PathVariable UUID id) {
        return adminReview.restoreReview(ReviewId.of(id));
    }
}