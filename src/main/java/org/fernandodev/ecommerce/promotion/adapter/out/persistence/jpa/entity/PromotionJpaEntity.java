package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.CustomerUsageEmbeddable;
import org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa.VolumeTierEmbeddable;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;

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
 * Entidad JPA del agregado {@code promotion}.
 */
@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
public class PromotionJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType type;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "percentage_value", precision = 5, scale = 2)
    private BigDecimal percentageValue;

    @Column(name = "fixed_amount", precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "fixed_currency", length = 3)
    private String fixedCurrency;

    @Column(name = "buy_quantity")
    private Integer buyQuantity;

    @Column(name = "get_quantity")
    private Integer getQuantity;

    @Column(name = "min_purchase_amount", precision = 19, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "min_purchase_currency", length = 3)
    private String minPurchaseCurrency;

    @Column(name = "max_discount_amount", precision = 19, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "max_discount_currency", length = 3)
    private String maxDiscountCurrency;

    @Column(name = "max_usage", nullable = false)
    private int maxUsage;

    @Column(name = "max_usage_per_customer", nullable = false)
    private int maxUsagePerCustomer;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_scope_product", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "product_id")
    private Set<UUID> productIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_scope_category", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "category_id")
    private Set<UUID> categoryIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_volume_tier", joinColumns = @JoinColumn(name = "promotion_id"))
    @OrderBy("minQuantity")
    private List<VolumeTierEmbeddable> volumeTiers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_usage", joinColumns = @JoinColumn(name = "promotion_id"))
    private List<CustomerUsageEmbeddable> usageByCustomer = new ArrayList<>();
}