package org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.StockMovementEmbeddable;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA del agregado {@code inventory}.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "on_hand", nullable = false)
    private int onHand;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    @Column(name = "minimum_stock", nullable = false)
    private int minimumStock;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "stock_movement", joinColumns = @JoinColumn(name = "inventory_id"))
    @OrderBy("at")
    private List<StockMovementEmbeddable> movements = new ArrayList<>();

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<StockReservationJpaEntity> reservations = new ArrayList<>();
}