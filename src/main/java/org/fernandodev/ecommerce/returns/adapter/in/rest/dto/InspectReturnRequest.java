package org.fernandodev.ecommerce.returns.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para registrar el resultado de la inspección de productos devueltos.
 */
public record InspectReturnRequest(
        @NotBlank @Size(max = 100) String condition,
        @Size(max = 500) String notes,
        boolean passed
) {
}