package org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa;

import java.time.Instant;

import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con una entrada del historial de estados del review.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReviewStatusHistoryEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status;

    @Column(name = "at", nullable = false)
    private Instant at;
}