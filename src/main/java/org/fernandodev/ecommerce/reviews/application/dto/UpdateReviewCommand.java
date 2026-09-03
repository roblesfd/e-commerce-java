package org.fernandodev.ecommerce.reviews.application.dto;

/**
 * Datos para actualizar un review.
 */
public record UpdateReviewCommand(int rating, String title, String comment) {
}