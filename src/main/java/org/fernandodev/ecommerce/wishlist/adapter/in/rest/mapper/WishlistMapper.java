package org.fernandodev.ecommerce.wishlist.adapter.in.rest.mapper;

import java.util.List;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.AddItemRequest;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.WishlistResponse;
import org.fernandodev.ecommerce.wishlist.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link Wishlist} y sus DTOs REST.
 */
@Component
public class WishlistMapper {

    public AddItemCommand toCommand(AddItemRequest request) {
        return new AddItemCommand(
                ProductId.of(request.productId()),
                request.variantId() == null ? null : ProductVariantId.of(request.variantId()));
    }

    public WishlistResponse toResponse(Wishlist wishlist) {
        List<WishlistResponse.ItemResponse> items = wishlist.getItems().stream()
                .map(item -> new WishlistResponse.ItemResponse(
                        item.productId().value(),
                        item.variantId() == null ? null : item.variantId().value(),
                        item.addedAt()))
                .toList();
        return new WishlistResponse(
                wishlist.getId().value(),
                wishlist.getCustomerId(),
                wishlist.getName(),
                items,
                wishlist.getCreatedAt(),
                wishlist.getUpdatedAt());
    }
}