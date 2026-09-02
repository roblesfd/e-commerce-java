package org.fernandodev.ecommerce.catalog.adapter.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear o actualizar una categoría.
 */
public record CategoryRequest(
        @NotBlank @Size(max = 120) String name,
        UUID parentId
) {
}