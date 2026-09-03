package org.fernandodev.ecommerce.wishlist.adapter.util.mapper;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa.WishlistItemEmbeddable;
import org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa.entity.WishlistJpaEntity;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistItem;

/**
 * Mapeo entre el agregado {@link Wishlist} y su entidad JPA.
 */
public final class WishlistPersistenceMapper {

    private WishlistPersistenceMapper() {
    }

    public static WishlistJpaEntity toEntity(Wishlist wishlist) {
        WishlistJpaEntity entity = new WishlistJpaEntity();
        entity.setId(wishlist.getId().value());
        entity.setCustomerId(wishlist.getCustomerId());
        entity.setName(wishlist.getName());
        entity.setCreatedAt(wishlist.getCreatedAt());
        entity.setItems(wishlist.getItems().stream()
                .map(WishlistPersistenceMapper::toItemEmbeddable)
                .toList());
        return entity;
    }

    public static Wishlist toDomain(WishlistJpaEntity entity) {
        List<WishlistItem> items = entity.getItems().stream()
                .map(WishlistPersistenceMapper::toWishlistItem)
                .toList();
        return Wishlist.crear(
                WishlistId.of(entity.getId()),
                entity.getCustomerId(),
                entity.getName(),
                items,
                entity.getCreatedAt(),
                entity.getUpdatedAt() == null ? entity.getCreatedAt() : entity.getUpdatedAt());
    }

    private static WishlistItemEmbeddable toItemEmbeddable(WishlistItem item) {
        WishlistItemEmbeddable embeddable = new WishlistItemEmbeddable();
        embeddable.setProductId(item.productId().value());
        embeddable.setVariantId(item.variantId() == null ? null : item.variantId().value());
        embeddable.setAddedAt(item.addedAt());
        return embeddable;
    }

    private static WishlistItem toWishlistItem(WishlistItemEmbeddable embeddable) {
        return new WishlistItem(
                ProductId.of(embeddable.getProductId()),
                embeddable.getVariantId() == null ? null : ProductVariantId.of(embeddable.getVariantId()),
                embeddable.getAddedAt());
    }
}