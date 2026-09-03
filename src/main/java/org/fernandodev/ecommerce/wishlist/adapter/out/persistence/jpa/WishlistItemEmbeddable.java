package org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con un item de la wishlist.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class WishlistItemEmbeddable {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}