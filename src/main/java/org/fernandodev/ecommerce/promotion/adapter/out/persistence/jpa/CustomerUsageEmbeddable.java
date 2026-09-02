package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con el uso de una promoción por cliente.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CustomerUsageEmbeddable {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "count", nullable = false)
    private int count;
}