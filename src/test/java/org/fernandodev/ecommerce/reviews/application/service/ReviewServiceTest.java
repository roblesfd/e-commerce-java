package org.fernandodev.ecommerce.reviews.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.reviews.application.dto.CreateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.dto.RatingSummary;
import org.fernandodev.ecommerce.reviews.application.dto.UpdateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.port.out.OrderPort;
import org.fernandodev.ecommerce.reviews.application.port.out.ReviewRepository;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ReviewServiceTest {

    private final FakeReviewRepository repository = new FakeReviewRepository();
    private final FakeOrderPort orders = new FakeOrderPort();
    private final ReviewService service = new ReviewService(repository, orders);

    private ProductId productId;
    private UUID customerId;

    @Test
    void createCreaReviewPendingModeration() {
        preparar();
        Review review = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(review.isVerifiedPurchase()).isFalse();
    }

    @Test
    void createMarcaCompraVerificadaSiOrdenEntregada() {
        preparar();
        orders.purchase = Optional.of(new OrderPort.VerifiedPurchase(OrderId.generate(), UUID.randomUUID()));

        Review review = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));

        assertThat(review.isVerifiedPurchase()).isTrue();
        assertThat(review.getOrderItemId()).isNotNull();
    }

    @Test
    void createDuplicadoLanzaConflicto() {
        preparar();
        repository.duplicado = true;

        assertThatThrownBy(() -> service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createConRatingInvalidoLanzaViolacion() {
        preparar();

        assertThatThrownBy(() -> service.create(customerId, productId,
                new CreateReviewCommand(7, "Excelente", "Muy bueno", null)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cicloModeracionViaServicio() {
        preparar();
        Review review = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));

        Review approved = service.approve(review.getId());
        assertThat(approved.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);

        Review hidden = service.hide(review.getId());
        assertThat(hidden.getStatus()).isEqualTo(ReviewStatus.HIDDEN);

        Review restored = service.restore(review.getId());
        assertThat(restored.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    }

    @Test
    void listByProductDevuelveSoloPublicados() {
        preparar();
        Review publicado = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));
        service.approve(publicado.getId());
        service.create(UUID.randomUUID(), productId,
                new CreateReviewCommand(4, "OK", "Aceptable", null));

        assertThat(service.listByProduct(productId)).hasSize(1);
    }

    @Test
    void listForModerationDevuelvePendientes() {
        preparar();
        Review pendiente = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));
        Review otro = service.create(UUID.randomUUID(), productId,
                new CreateReviewCommand(4, "OK", "Aceptable", null));
        service.approve(otro.getId());

        assertThat(service.listForModeration()).containsExactly(pendiente);
    }

    @Test
    void ratingSummaryCalculaPromedioYDistribucion() {
        preparar();
        Review a = service.create(customerId, productId,
                new CreateReviewCommand(5, "A", "C1", null));
        service.approve(a.getId());
        Review b = service.create(UUID.randomUUID(), productId,
                new CreateReviewCommand(5, "B", "C2", null));
        service.approve(b.getId());
        Review c = service.create(UUID.randomUUID(), productId,
                new CreateReviewCommand(4, "C", "C3", null));
        service.approve(c.getId());
        service.create(UUID.randomUUID(), productId,
                new CreateReviewCommand(1, "D", "C4", null));

        RatingSummary summary = service.ratingSummary(productId);

        assertThat(summary.count()).isEqualTo(3);
        assertThat(summary.average()).isEqualByComparingTo(new BigDecimal("4.67"));
        assertThat(summary.distribution()).containsEntry(5, 2L).containsEntry(4, 1L);
    }

    @Test
    void ratingSummarySinPublicadosDevuelveVacio() {
        preparar();

        RatingSummary summary = service.ratingSummary(productId);

        assertThat(summary.count()).isZero();
        assertThat(summary.average()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void updateDePublicadoVuelveAPendiente() {
        preparar();
        Review review = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));
        service.approve(review.getId());

        Review updated = service.update(review.getId(),
                new UpdateReviewCommand(4, "Nuevo", "Actualizado"));

        assertThat(updated.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(updated.getRating()).isEqualTo(4);
    }

    @Test
    void addReplyDevuelveLaRespuesta() {
        preparar();
        Review review = service.create(customerId, productId,
                new CreateReviewCommand(5, "Excelente", "Muy bueno", null));
        service.approve(review.getId());

        Review withReply = service.addReply(review.getId(), UUID.randomUUID(), "Gracias");

        assertThat(withReply.getReplies()).hasSize(1);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(ReviewId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void preparar() {
        customerId = UUID.randomUUID();
        productId = ProductId.generate();
    }

    private static final class FakeReviewRepository implements ReviewRepository {

        private final Map<ReviewId, Review> byId = new HashMap<>();
        private boolean duplicado;

        @Override
        public Review save(Review review) {
            byId.put(review.getId(), review);
            return review;
        }

        @Override
        public Optional<Review> findById(ReviewId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Review> findByProductId(ProductId productId) {
            return byId.values().stream().filter(r -> r.getProductId().equals(productId)).toList();
        }

        @Override
        public List<Review> findByCustomerId(UUID customerId) {
            return byId.values().stream().filter(r -> r.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public List<Review> findByStatus(ReviewStatus status) {
            return byId.values().stream().filter(r -> r.getStatus() == status).toList();
        }

        @Override
        public boolean existsByCustomerAndProduct(UUID customerId, ProductId productId) {
            return duplicado || byId.values().stream()
                    .anyMatch(r -> r.getCustomerId().equals(customerId) && r.getProductId().equals(productId));
        }

        @Override
        public List<Integer> findPublishedRatingsByProductId(ProductId productId) {
            List<Integer> ratings = new ArrayList<>();
            byId.values().stream()
                    .filter(r -> r.getProductId().equals(productId))
                    .filter(r -> r.getStatus() == ReviewStatus.PUBLISHED)
                    .forEach(r -> ratings.add(r.getRating()));
            return ratings;
        }
    }

    private static final class FakeOrderPort implements OrderPort {
        private Optional<VerifiedPurchase> purchase = Optional.empty();

        @Override
        public Optional<VerifiedPurchase> verifyPurchase(UUID customerId, ProductId productId) {
            return purchase;
        }
    }
}