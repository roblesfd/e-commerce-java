package org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa.PriceChangeEmbeddable;

/**
 * Entidad JPA del agregado {@code price}.
 */
@Entity
@Table(name = "price")
@Getter
@Setter
@NoArgsConstructor
public class PriceJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "price_change", joinColumns = @JoinColumn(name = "price_id"))
    @OrderBy("effectiveAt")
    private List<PriceChangeEmbeddable> history = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PriceOverrideJpaEntity> overrides = new ArrayList<>();
}