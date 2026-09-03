package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.ReturnItemEmbeddable;
import org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.ReturnStatusHistoryEmbeddable;
import org.fernandodev.ecommerce.returns.domain.value_object.RefundStatus;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;

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
 * Entidad JPA del agregado {@code return_request}.
 */
@Entity
@Table(name = "return_request")
@Getter
@Setter
@NoArgsConstructor
public class ReturnJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private ReturnReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReturnStatus status;

    @Column(name = "tracking_number", length = 128)
    private String trackingNumber;

    @Column(name = "refund_amount", precision = 19, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_currency", length = 3)
    private String refundCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", length = 20)
    private RefundStatus refundStatus;

    @Column(name = "refund_requested", nullable = false)
    private boolean refundRequested;

    @Column(name = "inspection_condition", length = 100)
    private String inspectionCondition;

    @Column(name = "inspection_notes", length = 500)
    private String inspectionNotes;

    @Column(name = "inspection_passed")
    private Boolean inspectionPassed;

    @Column(name = "inspection_at")
    private Instant inspectionAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "return_item", joinColumns = @JoinColumn(name = "return_id"))
    private List<ReturnItemEmbeddable> items = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "return_status_history", joinColumns = @JoinColumn(name = "return_id"))
    @OrderBy("at")
    private List<ReturnStatusHistoryEmbeddable> statusHistory = new ArrayList<>();
}