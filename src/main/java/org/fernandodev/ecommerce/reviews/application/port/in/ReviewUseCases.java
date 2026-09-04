package org.fernandodev.ecommerce.reviews.application.port.in;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.application.dto.CreateReviewCommand;
import org.fernandodev.ecommerce.reviews.application.dto.RatingSummary;
import org.fernandodev.ecommerce.reviews.application.dto.UpdateReviewCommand;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;

/**
 * Casos de uso de reviews (puerto de entrada).
 */
public interface ReviewUseCases {

    /**
     * Crea un review (verifica la compra y queda pendiente de moderación).
     */
    Review create(UUID customerId, ProductId productId, CreateReviewCommand command);

    /**
     * Actualiza un review (vuelve a moderación).
     */
    Review update(ReviewId id, UpdateReviewCommand command);

    /**
     * Elimina un review.
     */
    Review delete(ReviewId id);

    /**
     * Aprueba y publica un review.
     */
    Review approve(ReviewId id);

    /**
     * Rechaza un review.
     */
    Review reject(ReviewId id);

    /**
     * Oculta un review publicado.
     */
    Review hide(ReviewId id);

    /**
     * Restaura un review oculto.
     */
    Review restore(ReviewId id);

    /**
     * Añade una respuesta a un review publicado.
     */
    Review addReply(ReviewId id, UUID authorId, String comment);

    /**
     * Consulta un review por identificador.
     */
    Review getById(ReviewId id);

    /**
     * Lista los reviews publicados de un producto.
     */
    List<Review> listByProduct(ProductId productId);

    /**
     * Lista los reviews de un cliente.
     */
    List<Review> listByCustomer(UUID customerId);

    /**
     * Lista los reviews pendientes de moderación.
     */
    List<Review> listForModeration();

    /**
     * Calcula el resumen de rating de un producto.
     */
    RatingSummary ratingSummary(ProductId productId);
}