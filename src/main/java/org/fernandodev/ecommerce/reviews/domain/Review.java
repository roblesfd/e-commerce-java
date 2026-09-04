package org.fernandodev.ecommerce.reviews.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewReply;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatus;
import org.fernandodev.ecommerce.reviews.domain.value_object.ReviewStatusEntry;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una reseña de un producto.
 *
 * <p>Los reviews nuevos se crean en estado
 * {@link ReviewStatus#PENDING_MODERATION} y deben ser aprobados antes de
 * publicarse.</p>
 *
 * <p>Un review puede estar asociado opcionalmente a una variante de producto
 * y, cuando corresponde a una compra verificada, mantiene la referencia a la
 * orden y al item de orden que originaron la reseña.</p>
 *
 * <p>El agregado también gestiona las respuestas asociadas al review y
 * mantiene un historial de sus cambios de estado.</p>
 *
 * <p>El rating debe encontrarse siempre dentro del rango definido por
 * {@link #MIN_RATING} y {@link #MAX_RATING}.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {

    /**
     * Valor mínimo permitido para la valoración de un review.
     */
    public static final int MIN_RATING = 1;

    /**
     * Valor máximo permitido para la valoración de un review.
     */
    public static final int MAX_RATING = 5;

    /**
     * Identificador único del review.
     */
    private ReviewId id;

    /**
     * Identificador del cliente que creó el review.
     */
    private UUID customerId;

    /**
     * Identificador del producto reseñado.
     */
    private ProductId productId;

    /**
     * Identificador de la variante específica del producto reseñado.
     *
     * <p>Este valor es opcional cuando el review aplica al producto de forma
     * general y no a una variante específica.</p>
     */
    private ProductVariantId variantId;

    /**
     * Identificador de la orden asociada al review cuando corresponde
     * a una compra verificada.
     */
    private OrderId orderId;

    /**
     * Identificador del item de la orden que originó el review cuando
     * corresponde a una compra verificada.
     */
    private UUID orderItemId;

    /**
     * Valoración numérica otorgada al producto.
     *
     * <p>El valor debe encontrarse entre {@link #MIN_RATING} y
     * {@link #MAX_RATING}, ambos inclusive.</p>
     */
    private int rating;

    /**
     * Título de la reseña.
     */
    private String title;

    /**
     * Contenido textual de la reseña.
     */
    private String comment;

    /**
     * Estado actual del review dentro de su ciclo de vida.
     */
    private ReviewStatus status;

    /**
     * Indica si el review corresponde a una compra verificada.
     *
     * <p>Cuando es {@code true}, el review mantiene las referencias a la
     * orden y al item de orden que originaron la compra.</p>
     */
    private boolean verifiedPurchase;

    /**
     * Respuestas asociadas al review.
     *
     * <p>La colección se expone mediante {@link #getReplies()} como una
     * colección no modificable para proteger el estado interno del agregado.</p>
     */
    @Getter(AccessLevel.NONE)
    private List<ReviewReply> replies;

    /**
     * Historial de las transiciones de estado del review.
     *
     * <p>La colección se expone mediante {@link #getStatusHistory()} como
     * una colección no modificable.</p>
     */
    @Getter(AccessLevel.NONE)
    private List<ReviewStatusEntry> statusHistory;

    /**
     * Fecha y hora en la que se creó el review.
     */
    private Instant createdAt;

    /**
     * Fecha y hora de la última modificación del review.
     */
    private Instant updatedAt;

    /**
     * Crea un nuevo review en estado {@link ReviewStatus#PENDING_MODERATION}.
     *
     * <p>Los reviews creados mediante esta factoría no se consideran compras
     * verificadas y no tienen inicialmente una orden o item de orden asociado.</p>
     *
     * @param id identificador único del review.
     * @param customerId identificador del cliente que crea el review.
     * @param productId identificador del producto reseñado.
     * @param variantId identificador de la variante reseñada, si aplica.
     * @param rating valoración numérica del producto.
     * @param title título del review.
     * @param comment contenido del review.
     * @param at instante en el que se crea el review.
     * @return una nueva instancia de {@code Review}.
     * @throws NullPointerException si alguno de los parámetros obligatorios
     *         es {@code null}.
     * @throws BusinessRuleViolationException si el rating se encuentra
     *         fuera del rango permitido.
     */
    public static Review create(
            ReviewId id,
            UUID customerId,
            ProductId productId,
            ProductVariantId variantId,
            int rating,
            String title,
            String comment,
            Instant at
    ) {
        Review review = new Review();

        review.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );
        review.customerId = Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );
        review.productId = Objects.requireNonNull(
                productId,
                "productId no puede ser null"
        );
        review.variantId = variantId;
        review.orderId = null;
        review.orderItemId = null;
        review.rating = validateRating(rating);
        review.title = title;
        review.comment = Objects.requireNonNull(
                comment,
                "comment no puede ser null"
        );
        review.status = ReviewStatus.PENDING_MODERATION;
        review.verifiedPurchase = false;
        review.replies = new ArrayList<>();
        review.statusHistory = new ArrayList<>(
                List.of(
                        new ReviewStatusEntry(
                                ReviewStatus.PENDING_MODERATION,
                                at
                        )
                )
        );
        review.createdAt = Objects.requireNonNull(
                at,
                "at no puede ser null"
        );
        review.updatedAt = at;

        return review;
    }

    /**
     * Reconstruye un review existente a partir de su estado completo.
     *
     * <p>Este método está destinado principalmente a la reconstrucción del
     * agregado desde la información proporcionada por la infraestructura
     * de persistencia.</p>
     *
     * <p>Las colecciones recibidas se copian defensivamente para evitar
     * compartir referencias mutables con el exterior.</p>
     *
     * @param id identificador único del review.
     * @param customerId identificador del cliente.
     * @param productId identificador del producto reseñado.
     * @param variantId identificador de la variante reseñada.
     * @param orderId identificador de la orden asociada.
     * @param orderItemId identificador del item de la orden asociado.
     * @param rating valoración numérica del producto.
     * @param title título del review.
     * @param comment contenido del review.
     * @param status estado actual del review.
     * @param verifiedPurchase indica si corresponde a una compra verificada.
     * @param replies respuestas asociadas al review.
     * @param statusHistory historial de estados del review.
     * @param createdAt fecha y hora de creación.
     * @param updatedAt fecha y hora de última modificación.
     * @return una instancia de {@code Review} reconstruida con el estado
     *         proporcionado.
     * @throws NullPointerException si alguno de los campos obligatorios
     *         es {@code null}.
     * @throws BusinessRuleViolationException si el rating se encuentra
     *         fuera del rango permitido.
     */
    public static Review crear(
            ReviewId id,
            UUID customerId,
            ProductId productId,
            ProductVariantId variantId,
            OrderId orderId,
            UUID orderItemId,
            int rating,
            String title,
            String comment,
            ReviewStatus status,
            boolean verifiedPurchase,
            List<ReviewReply> replies,
            List<ReviewStatusEntry> statusHistory,
            Instant createdAt,
            Instant updatedAt
    ) {
        Review review = new Review();

        review.id = Objects.requireNonNull(
                id,
                "id no puede ser null"
        );
        review.customerId = Objects.requireNonNull(
                customerId,
                "customerId no puede ser null"
        );
        review.productId = Objects.requireNonNull(
                productId,
                "productId no puede ser null"
        );
        review.variantId = variantId;
        review.orderId = orderId;
        review.orderItemId = orderItemId;
        review.rating = validateRating(rating);
        review.title = title;
        review.comment = Objects.requireNonNull(
                comment,
                "comment no puede ser null"
        );
        review.status = Objects.requireNonNull(
                status,
                "status no puede ser null"
        );
        review.verifiedPurchase = verifiedPurchase;
        review.replies = new ArrayList<>(
                replies == null ? List.of() : replies
        );
        review.statusHistory = new ArrayList<>(
                statusHistory == null ? List.of() : statusHistory
        );
        review.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt no puede ser null"
        );
        review.updatedAt = Objects.requireNonNull(
                updatedAt,
                "updatedAt no puede ser null"
        );

        return review;
    }

    /**
     * Obtiene las respuestas asociadas al review.
     *
     * <p>La colección devuelta no puede modificarse directamente, protegiendo
     * así el estado interno del agregado.</p>
     *
     * @return lista no modificable de respuestas.
     */
    public List<ReviewReply> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    /**
     * Obtiene el historial de estados del review.
     *
     * <p>La colección devuelta no puede modificarse directamente.</p>
     *
     * @return lista no modificable con el historial de estados.
     */
    public List<ReviewStatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    /**
     * Marca el review como asociado a una compra verificada.
     *
     * <p>La operación es idempotente: si el review ya está marcado como
     * compra verificada, no realiza ninguna modificación.</p>
     *
     * <p>Al verificar la compra se registran la orden y el item de orden
     * que originaron el review.</p>
     *
     * @param orderId identificador de la orden que originó la compra.
     * @param orderItemId identificador del item de la orden que originó
     *                    el review.
     * @param at instante en el que se verifica la compra.
     * @throws NullPointerException si {@code orderId}, {@code orderItemId}
     *         o {@code at} son {@code null}.
     */
    public void markVerifiedPurchase(
            OrderId orderId,
            UUID orderItemId,
            Instant at
    ) {
        if (this.verifiedPurchase) {
            return;
        }

        this.verifiedPurchase = true;
        this.orderId = Objects.requireNonNull(
                orderId,
                "orderId no puede ser null"
        );
        this.orderItemId = Objects.requireNonNull(
                orderItemId,
                "orderItemId no puede ser null"
        );
        this.updatedAt = at;
    }

    /**
     * Actualiza el contenido y la valoración del review.
     *
     * <p>Solo pueden actualizarse reviews que se encuentren en estado
     * {@link ReviewStatus#PENDING_MODERATION} o
     * {@link ReviewStatus#PUBLISHED}.</p>
     *
     * <p>Si un review publicado es modificado, vuelve a
     * {@link ReviewStatus#PENDING_MODERATION} para requerir una nueva
     * moderación.</p>
     *
     * @param title nuevo título del review.
     * @param comment nuevo contenido del review.
     * @param rating nueva valoración del producto.
     * @param at instante en el que se actualiza el review.
     * @throws NullPointerException si {@code comment} o {@code at}
     *         son {@code null}.
     * @throws BusinessRuleViolationException si el estado actual no permite
     *         modificar el review o el rating está fuera del rango permitido.
     */
    public void update(
            String title,
            String comment,
            int rating,
            Instant at
    ) {
        if (status != ReviewStatus.PENDING_MODERATION
                && status != ReviewStatus.PUBLISHED) {
            throw new BusinessRuleViolationException(
                    "Solo se puede actualizar un review "
                            + "PENDING_MODERATION o PUBLISHED"
            );
        }

        this.title = title;
        this.comment = Objects.requireNonNull(
                comment,
                "comment no puede ser null"
        );
        this.rating = validateRating(rating);
        this.updatedAt = at;

        if (status == ReviewStatus.PUBLISHED) {
            transition(
                    ReviewStatus.PENDING_MODERATION,
                    at
            );
        }
    }

    /**
     * Aprueba el review y lo publica.
     *
     * <p>Un review puede publicarse cuando se encuentra pendiente de
     * moderación o cuando ha sido restaurado desde un estado oculto.</p>
     *
     * @param at instante en el que se aprueba el review.
     * @throws BusinessRuleViolationException si el estado actual no permite
     *         publicar el review.
     */
    public void approve(Instant at) {
        requireTransition(
                ReviewStatus.PUBLISHED,
                "aprobar",
                at
        );
    }

    /**
     * Rechaza un review pendiente de moderación.
     *
     * @param at instante en el que se rechaza el review.
     * @throws BusinessRuleViolationException si el review no se encuentra
     *         en estado {@link ReviewStatus#PENDING_MODERATION}.
     */
    public void reject(Instant at) {
        requireTransition(
                ReviewStatus.REJECTED,
                "rechazar",
                at
        );
    }

    /**
     * Oculta un review publicado.
     *
     * <p>La operación retira el review de su estado publicado sin eliminar
     * el agregado.</p>
     *
     * @param at instante en el que se oculta el review.
     * @throws BusinessRuleViolationException si el review no se encuentra
     *         publicado.
     */
    public void hide(Instant at) {
        requireTransition(
                ReviewStatus.HIDDEN,
                "ocultar",
                at
        );
    }

    /**
     * Restaura un review oculto y lo vuelve a publicar.
     *
     * @param at instante en el que se restaura el review.
     * @throws BusinessRuleViolationException si el review no se encuentra
     *         en estado {@link ReviewStatus#HIDDEN}.
     */
    public void restore(Instant at) {
        requireTransition(
                ReviewStatus.PUBLISHED,
                "restaurar",
                at
        );
    }

    /**
     * Marca el review como eliminado.
     *
     * <p>La eliminación es representada mediante el estado
     * {@link ReviewStatus#DELETED} en lugar de eliminar físicamente
     * el agregado.</p>
     *
     * @param at instante en el que se elimina el review.
     * @throws BusinessRuleViolationException si el review ya se encuentra
     *         eliminado.
     */
    public void delete(Instant at) {
        if (status == ReviewStatus.DELETED) {
            throw new BusinessRuleViolationException(
                    "El review ya está eliminado"
            );
        }

        transition(
                ReviewStatus.DELETED,
                at
        );
    }

    /**
     * Agrega una respuesta al review.
     *
     * <p>Las respuestas únicamente pueden agregarse cuando el review
     * se encuentra publicado.</p>
     *
     * @param reply respuesta que se desea asociar al review.
     * @throws NullPointerException si {@code reply} es {@code null}.
     * @throws BusinessRuleViolationException si el review no se encuentra
     *         publicado.
     */
    public void addReply(ReviewReply reply) {
        if (status != ReviewStatus.PUBLISHED) {
            throw new BusinessRuleViolationException(
                    "Solo se puede responder a un review publicado"
            );
        }

        replies.add(
                Objects.requireNonNull(
                        reply,
                        "reply no puede ser null"
                )
        );

        this.updatedAt = reply.at();
    }

    /**
     * Determina si el review se encuentra en un estado terminal.
     *
     * <p>Los estados terminales son {@link ReviewStatus#REJECTED} y
     * {@link ReviewStatus#DELETED}.</p>
     *
     * @return {@code true} si el review se encuentra en un estado terminal;
     *         {@code false} en caso contrario.
     */
    public boolean isTerminal() {
        return status == ReviewStatus.REJECTED
                || status == ReviewStatus.DELETED;
    }

    /**
     * Valida que una transición de estado sea válida y, en caso afirmativo,
     * la ejecuta.
     *
     * <p>Las transiciones permitidas son:</p>
     *
     * <ul>
     *     <li>{@code PENDING_MODERATION -> PUBLISHED}</li>
     *     <li>{@code HIDDEN -> PUBLISHED}</li>
     *     <li>{@code PENDING_MODERATION -> REJECTED}</li>
     *     <li>{@code PUBLISHED -> HIDDEN}</li>
     * </ul>
     *
     * @param target estado al que se desea realizar la transición.
     * @param operacion descripción de la operación que origina la transición.
     * @param at instante en el que se realiza la transición.
     * @throws BusinessRuleViolationException si la transición no está
     *         permitida desde el estado actual.
     */
    private void requireTransition(
            ReviewStatus target,
            String operacion,
            Instant at
    ) {
        boolean valid = switch (target) {
            case PUBLISHED ->
                    status == ReviewStatus.PENDING_MODERATION
                            || status == ReviewStatus.HIDDEN;
            case REJECTED ->
                    status == ReviewStatus.PENDING_MODERATION;
            case HIDDEN ->
                    status == ReviewStatus.PUBLISHED;
            default -> false;
        };

        if (!valid) {
            throw new BusinessRuleViolationException(
                    "No se puede "
                            + operacion
                            + " un review en estado "
                            + status
            );
        }

        transition(
                target,
                at
        );
    }

    /**
     * Ejecuta una transición de estado y registra el cambio en el historial.
     *
     * <p>Actualiza el estado actual, la fecha de modificación y agrega
     * una nueva entrada al historial del agregado.</p>
     *
     * @param target nuevo estado del review.
     * @param at instante en el que se realiza la transición.
     */
    private void transition(
            ReviewStatus target,
            Instant at
    ) {
        this.status = target;
        this.updatedAt = at;
        statusHistory.add(
                new ReviewStatusEntry(
                        target,
                        at
                )
        );
    }

    /**
     * Valida que una valoración se encuentre dentro del rango permitido.
     *
     * @param rating valoración que se desea validar.
     * @return el rating validado.
     * @throws BusinessRuleViolationException si el rating es menor que
     *         {@link #MIN_RATING} o mayor que {@link #MAX_RATING}.
     */
    private static int validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessRuleViolationException(
                    "El rating debe estar entre 1 y 5"
            );
        }

        return rating;
    }
}
