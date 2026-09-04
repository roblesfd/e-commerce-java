package org.fernandodev.ecommerce.reviews.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;

/**
 * Puerto de salida de persistencia del agregado {@link Review}.
 */
public interface ReviewRepository {

    /**
     * Persiste el review.
     */
    Review save(Review review);

    /**
     * Consulta un review por identificador.
     */
    Optional<Review> findById(ReviewId id);

    /**
     * Lista los reviews de un producto.
     */
    List<Review> findByProductId(ProductId productId);

    /**
     * Lista los reviews de un cliente.
     */
    List<Review> findByCustomerId(UUID customerId);

    /**
     * Lista los reviews por estado.
     */
    List<Review> findByStatus(ReviewStatus status);

    /**
     * Indica si el cliente ya reviewó el producto.
     */
    boolean existsByCustomerAndProduct(UUID customerId, ProductId productId);

    /**
     * Devuelve los ratings de los reviews publicados de un producto.
     */
    List<Integer> findPublishedRatingsByProductId(ProductId productId);
}