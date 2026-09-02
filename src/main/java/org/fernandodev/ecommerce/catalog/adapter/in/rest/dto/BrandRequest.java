package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear o actualizar una marca.
 */
public record BrandRequest(
        @NotBlank @Size(max = 120) String name
) {
}