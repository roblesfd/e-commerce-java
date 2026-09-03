package org.fernandodev.ecommerce.reviews.adapter.out.persistence.jpa;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con una respuesta al review.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReviewReplyEmbeddable {

    @Column(name = "reply_id", nullable = false)
    private UUID replyId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "comment", nullable = false, length = 1000)
    private String comment;

    @Column(name = "at", nullable = false)
    private Instant at;
}