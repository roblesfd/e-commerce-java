package org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa;

import java.time.Instant;

import org.fernandodev.ecommerce.inventory.domain.value_object.StockMovementType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con un movimiento de inventario (auditoría).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class StockMovementEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private StockMovementType type;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "at", nullable = false)
    private Instant at;
}