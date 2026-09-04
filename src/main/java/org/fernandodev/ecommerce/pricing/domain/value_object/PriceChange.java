package org.fernandodev.ecommerce.pricing.domain.value_object;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Entrada del historial de un precio: captura el valor anterior cuando el
 * precio base cambia.
 *
 * @param amount       importe del precio anterior.
 * @param effectiveAt  momento en que dejó de estar vigente.
 */
public record PriceChange(Money amount, Instant effectiveAt) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code amount} o {@code effectiveAt} son {@code null}.
     */
    public PriceChange {
        Objects.requireNonNull(amount, "amount no puede ser null");
        Objects.requireNonNull(effectiveAt, "effectiveAt no puede ser null");
    }
}