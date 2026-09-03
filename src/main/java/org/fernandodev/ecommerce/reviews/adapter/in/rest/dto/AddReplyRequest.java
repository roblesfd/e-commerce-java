package org.fernandodev.ecommerce.reviews.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para responder a un review.
 */
public record AddReplyRequest(
        @NotBlank @Size(max = 1000) String comment
) {
}