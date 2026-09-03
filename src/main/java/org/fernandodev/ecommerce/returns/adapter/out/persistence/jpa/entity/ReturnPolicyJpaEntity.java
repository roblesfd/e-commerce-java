package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA del agregado {@code return_policy}.
 */
@Entity
@Table(name = "return_policy")
@Getter
@Setter
@NoArgsConstructor
public class ReturnPolicyJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "return_window_days", nullable = false)
    private int returnWindowDays;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "return_policy_product", joinColumns = @JoinColumn(name = "return_policy_id"))
    @Column(name = "product_id")
    private Set<UUID> returnableProductIds = new HashSet<>();
}