package org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con la entrada del historial de precios (valor anterior a un
 * cambio del precio base).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PriceChangeEmbeddable {

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}