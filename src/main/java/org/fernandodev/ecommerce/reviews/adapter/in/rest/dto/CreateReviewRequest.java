package org.fernandodev.ecommerce.reviews.adapter.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear un review.
 */
public record CreateReviewRequest(
        @NotNull UUID productId,
        UUID variantId,
        @Min(1) @Max(5) int rating,
        @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String comment
) {
}