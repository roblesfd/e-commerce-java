package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import java.time.Instant;

import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con una entrada del historial de estados del envío.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ShipmentStatusHistoryEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(name = "at", nullable = false)
    private Instant at;
}