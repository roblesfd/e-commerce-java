package org.fernandodev.ecommerce.reviews.domain.value_object;

/**
 * Representa los estados que puede atravesar un {@code Review}
 * durante su ciclo de vida dentro del sistema.
 *
 * <p>El estado determina la situación actual de la reseña y permite
 * controlar las transiciones relacionadas con su moderación,
 * publicación, ocultamiento y eliminación.</p>
 *
 * <p>El ciclo de vida habitual de una reseña comienza en
 * {@link #PENDING_MODERATION}, desde donde puede ser publicada o
 * rechazada. Una reseña publicada puede posteriormente ser ocultada
 * o eliminada de acuerdo con las reglas del dominio.</p>
 */
public enum ReviewStatus {

    /**
     * Indica que la reseña fue creada y está pendiente de ser
     * revisada por el proceso de moderación.
     */
    PENDING_MODERATION,

    /**
     * Indica que la reseña fue aprobada y se encuentra visible
     * para los usuarios.
     */
    PUBLISHED,

    /**
     * Indica que la reseña fue rechazada durante el proceso
     * de moderación y, por tanto, no puede ser publicada.
     */
    REJECTED,

    /**
     * Indica que la reseña fue publicada previamente, pero
     * actualmente se encuentra oculta para los usuarios.
     *
     * <p>El contenido permanece registrado en el sistema, aunque
     * no se muestra públicamente.</p>
     */
    HIDDEN,

    /**
     * Indica que la reseña fue eliminada y ha alcanzado un estado
     * terminal dentro de su ciclo de vida.
     */
    DELETED
}
