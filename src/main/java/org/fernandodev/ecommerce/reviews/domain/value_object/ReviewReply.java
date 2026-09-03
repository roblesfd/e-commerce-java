package org.fernandodev.ecommerce.reviews.domain.value_object;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Respuesta a un review.
 *
 * @param replyId  identificador de la respuesta.
 * @param authorId autor de la respuesta.
 * @param comment  contenido.
 * @param at       momento de la respuesta.
 */
public record ReviewReply(UUID replyId, UUID authorId, String comment, Instant at) {

    public ReviewReply {
        Objects.requireNonNull(replyId, "replyId no puede ser null");
        Objects.requireNonNull(authorId, "authorId no puede ser null");
        Objects.requireNonNull(comment, "comment no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}