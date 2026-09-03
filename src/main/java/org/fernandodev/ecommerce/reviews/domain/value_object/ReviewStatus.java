package org.fernandodev.ecommerce.reviews.domain.value_object;

/**
 * Ciclo de vida de un review.
 */
public enum ReviewStatus {

    PENDING_MODERATION,
    PUBLISHED,
    REJECTED,
    HIDDEN,
    DELETED
}