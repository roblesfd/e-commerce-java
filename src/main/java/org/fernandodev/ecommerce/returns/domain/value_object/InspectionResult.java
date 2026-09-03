package org.fernandodev.ecommerce.returns.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado de la inspección de los productos devueltos.
 *
 * @param condition condición reportada.
 * @param notes     observaciones (opcional).
 * @param passed    si la inspección fue aprobada.
 * @param at        momento de la inspección.
 */
public record InspectionResult(String condition, String notes, boolean passed, Instant at) {

    public InspectionResult {
        Objects.requireNonNull(condition, "condition no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}