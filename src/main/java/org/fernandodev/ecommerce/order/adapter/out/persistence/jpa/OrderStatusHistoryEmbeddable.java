package org.fernandodev.ecommerce.order.adapter.out.persistence.jpa;

import java.time.Instant;

import org.fernandodev.ecommerce.order.domain.value_object.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con una entrada del historial de estados de la orden.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistoryEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "at", nullable = false)
    private Instant at;
}