package org.fernandodev.ecommerce.pricing.domain.value_object;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Precio promocional o temporal que se aplica en una ventana de tiempo sobre
 * un precio.
 *
 * @param id         identificador del override.
 * @param amount     importe aplicable durante la ventana.
 * @param validFrom  inicio de la ventana (inclusive).
 * @param validTo    fin de la ventana (exclusive).
 * @param type       tipo de override (promocional o temporal).
 */
public record PriceOverride(
        PriceOverrideId id,
        Money amount,
        Instant validFrom,
        Instant validTo,
        PriceType type
) {

    public PriceOverride {
        Objects.requireNonNull(id, "id no puede ser null");
        Objects.requireNonNull(amount, "amount no puede ser null");
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");
        Objects.requireNonNull(type, "type no puede ser null");
        if (!validFrom.isBefore(validTo)) {
            throw new IllegalArgumentException("validFrom debe ser anterior a validTo");
        }
    }

    /**
     * Indica si esta ventana se solapa con el intervalo {@code [from, to)}.
     */
    public boolean overlaps(Instant from, Instant to) {
        return validFrom.isBefore(to) && from.isBefore(validTo);
    }
}