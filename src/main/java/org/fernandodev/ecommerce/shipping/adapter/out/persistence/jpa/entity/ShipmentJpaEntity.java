package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.ShipmentStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;

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
 * Entidad JPA del agregado {@code shipment}.
 */
@Entity
@Table(name = "shipment")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "shipping_method_id", nullable = false)
    private UUID shippingMethodId;

    @Column(name = "address_id")
    private UUID addressId;

    @Column(name = "address_snapshot", length = 500)
    private String addressSnapshot;

    @Column(name = "tracking_number", length = 128)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(name = "cost_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shipment_status_history", joinColumns = @JoinColumn(name = "shipment_id"))
    @OrderBy("at")
    private List<ShipmentStatusHistoryEmbeddable> statusHistory = new ArrayList<>();
}