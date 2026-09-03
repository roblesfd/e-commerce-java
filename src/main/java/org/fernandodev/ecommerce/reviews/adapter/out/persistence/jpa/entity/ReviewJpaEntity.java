package org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.ReviewReplyEmbeddable;
import org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa.ReviewStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA del agregado {@code review}.
 */
@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class ReviewJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_item_id")
    private UUID orderItemId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "comment", nullable = false, length = 2000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status;

    @Column(name = "verified_purchase", nullable = false)
    private boolean verifiedPurchase;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "review_status_history", joinColumns = @JoinColumn(name = "review_id"))
    @OrderBy("at")
    private List<ReviewStatusHistoryEmbeddable> statusHistory = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "review_reply", joinColumns = @JoinColumn(name = "review_id"))
    private List<ReviewReplyEmbeddable> replies = new ArrayList<>();
}