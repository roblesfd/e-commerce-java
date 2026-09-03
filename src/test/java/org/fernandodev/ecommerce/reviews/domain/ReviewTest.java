package org.fernandodev.ecommerce.reviews.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewReply;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class ReviewTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaReviewPendingModeration() {
        Review review = review();

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(review.isVerifiedPurchase()).isFalse();
        assertThat(review.getStatusHistory()).hasSize(1);
        assertThat(review.isTerminal()).isFalse();
    }

    @Test
    void createConRatingInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> Review.create(ReviewId.generate(), UUID.randomUUID(), ProductId.generate(),
                null, 0, "Título", "Buen producto", T1))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> Review.create(ReviewId.generate(), UUID.randomUUID(), ProductId.generate(),
                null, 6, "Título", "Buen producto", T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void markVerifiedPurchaseAsociaOrderItemYEsIdempotente() {
        Review review = review();
        OrderId orderId = OrderId.generate();
        UUID orderItemId = UUID.randomUUID();

        review.markVerifiedPurchase(orderId, orderItemId, T1);
        review.markVerifiedPurchase(OrderId.generate(), UUID.randomUUID(), T1);

        assertThat(review.isVerifiedPurchase()).isTrue();
        assertThat(review.getOrderId()).isEqualTo(orderId);
        assertThat(review.getOrderItemId()).isEqualTo(orderItemId);
    }

    @Test
    void cicloModeracionYVisibilidad() {
        Review review = review();

        review.approve(T1);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);

        review.hide(T1);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);

        review.restore(T1);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);
        assertThat(review.getStatusHistory()).hasSize(4);
    }

    @Test
    void rejectDesdePendingModeration() {
        Review review = review();
        review.reject(T1);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.REJECTED);
        assertThat(review.isTerminal()).isTrue();
    }

    @Test
    void approveDesdeRechazadoLanzaExcepcion() {
        Review review = review();
        review.reject(T1);

        assertThatThrownBy(() -> review.approve(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void deleteDesdeCualquierEstado() {
        Review pendiente = review();
        pendiente.delete(T1);
        assertThat(pendiente.getStatus()).isEqualTo(ReviewStatus.DELETED);

        Review publicado = review();
        publicado.approve(T1);
        publicado.delete(T1);
        assertThat(publicado.getStatus()).isEqualTo(ReviewStatus.DELETED);

        assertThatThrownBy(() -> publicado.delete(T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateDeReviewPublicadoVuelveAPendienteModeracion() {
        Review review = review();
        review.approve(T1);

        review.update("Nuevo título", "Nuevo comentario", 4, T1);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.getComment()).isEqualTo("Nuevo comentario");
    }

    @Test
    void updateDeReviewRechazadoLanzaExcepcion() {
        Review review = review();
        review.reject(T1);

        assertThatThrownBy(() -> review.update("T", "C", 3, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addReplySoloCuandoPublicado() {
        Review review = review();
        assertThatThrownBy(() -> review.addReply(respuesta()))
                .isInstanceOf(BusinessRuleViolationException.class);

        review.approve(T1);
        review.addReply(respuesta());

        assertThat(review.getReplies()).hasSize(1);
    }

    private static Review review() {
        return Review.create(ReviewId.generate(), UUID.randomUUID(), ProductId.generate(),
                null, 5, "Excelente", "Muy buen producto", T1);
    }

    private static ReviewReply respuesta() {
        return new ReviewReply(UUID.randomUUID(), UUID.randomUUID(), "Gracias por tu comentario", T1);
    }
}