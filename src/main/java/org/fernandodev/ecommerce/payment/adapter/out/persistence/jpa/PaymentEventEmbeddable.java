package org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con un evento del historial del pago.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PaymentEventEmbeddable {

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "at", nullable = false)
    private Instant at;
}