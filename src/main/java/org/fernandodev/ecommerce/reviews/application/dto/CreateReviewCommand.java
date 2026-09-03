package org.fernandodev.ecommerce.reviews.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Datos para crear un review.
 */
public record CreateReviewCommand(int rating, String title, String comment, ProductVariantId variantId) {
}