package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA del agregado {@code coupon}.
 */
@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
public class CouponJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "promotion_id", nullable = false)
    private UUID promotionId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}