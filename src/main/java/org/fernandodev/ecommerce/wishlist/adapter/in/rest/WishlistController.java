package org.fernandodev.ecommerce.wishlist.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.infrastructure.security.SecurityUtils;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.AddItemRequest;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.CreateWishlistRequest;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.dto.WishlistResponse;
import org.fernandodev.ecommerce.wishlist.adapter.in.rest.mapper.WishlistMapper;
import org.fernandodev.ecommerce.wishlist.application.port.in.WishlistUseCases;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de wishlists.
 */
@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistUseCases wishlistUseCases;
    private final WishlistMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistResponse create(@Valid @RequestBody CreateWishlistRequest request) {
        return mapper.toResponse(wishlistUseCases.create(SecurityUtils.requiredUserId(), request.name()));
    }

    @GetMapping
    /**
     * Mine.
     */
    public List<WishlistResponse> mine() {
        return wishlistUseCases.listByCustomer(SecurityUtils.requiredUserId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public WishlistResponse get(@PathVariable UUID id) {
        return mapper.toResponse(wishlistUseCases.getById(WishlistId.of(id)));
    }

    @PatchMapping("/{id}")
    /**
     * Rename.
     * @param id id.
     * @param request request.
     */
    public WishlistResponse rename(@PathVariable UUID id, @Valid @RequestBody CreateWishlistRequest request) {
        return mapper.toResponse(wishlistUseCases.rename(WishlistId.of(id), request.name()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Delete.
     * @param id id.
     */
    public void delete(@PathVariable UUID id) {
        wishlistUseCases.delete(WishlistId.of(id));
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    /**
     * Add item.
     * @param id id.
     * @param request request.
     */
    public WishlistResponse addItem(@PathVariable UUID id, @Valid @RequestBody AddItemRequest request) {
        return mapper.toResponse(wishlistUseCases.addItem(WishlistId.of(id), mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Remove item.
     * @param id id.
     * @param productId productId.
     * @param variantId variantId.
     */
    public void removeItem(@PathVariable UUID id, @PathVariable UUID productId,
                           @RequestParam(required = false) UUID variantId) {
        wishlistUseCases.removeItem(WishlistId.of(id), ProductId.of(productId),
                variantId == null ? null : ProductVariantId.of(variantId));
    }

    @DeleteMapping("/{id}/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Clear.
     * @param id id.
     */
    public void clear(@PathVariable UUID id) {
        wishlistUseCases.clear(WishlistId.of(id));
    }
}