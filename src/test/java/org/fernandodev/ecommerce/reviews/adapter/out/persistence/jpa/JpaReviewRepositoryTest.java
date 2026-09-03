package org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.application.port.out.ReviewRepository;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaReviewRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ReviewRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaReviewConHistorial() {
        UUID customerId = UUID.randomUUID();
        Review review = Review.create(ReviewId.generate(), customerId, ProductId.generate(),
                null, 5, "Excelente", "Muy bueno", AT);

        repository.save(review);
        Review loaded = repository.findById(review.getId()).orElseThrow();

        assertThat(loaded.getRating()).isEqualTo(5);
        assertThat(loaded.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(loaded.getStatusHistory()).hasSize(1);
    }

    @Test
    void existsByCustomerAndProductDevuelveTrue() {
        UUID customerId = UUID.randomUUID();
        ProductId productId = ProductId.generate();
        repository.save(Review.create(ReviewId.generate(), customerId, productId, null, 4, "OK", "Bien", AT));

        assertThat(repository.existsByCustomerAndProduct(customerId, productId)).isTrue();
        assertThat(repository.existsByCustomerAndProduct(UUID.randomUUID(), productId)).isFalse();
    }

    @Test
    void findByStatusYFindPublishedRatings() {
        ProductId productId = ProductId.generate();
        Review publicado = Review.create(ReviewId.generate(), UUID.randomUUID(), productId,
                null, 5, "A", "Excelente", AT);
        publicado.approve(AT);
        Review pendiente = Review.create(ReviewId.generate(), UUID.randomUUID(), productId,
                null, 3, "B", "Regular", AT);
        repository.save(publicado);
        repository.save(pendiente);

        assertThat(repository.findByStatus(ReviewStatus.PUBLISHED)).hasSize(1);
        assertThat(repository.findPublishedRatingsByProductId(productId)).containsExactly(5);
    }

    @Test
    void findByProductIdDevuelveLosDelProducto() {
        ProductId productId = ProductId.generate();
        repository.save(Review.create(ReviewId.generate(), UUID.randomUUID(), productId,
                null, 5, "A", "Excelente", AT));

        assertThat(repository.findByProductId(productId)).hasSize(1);
    }
}