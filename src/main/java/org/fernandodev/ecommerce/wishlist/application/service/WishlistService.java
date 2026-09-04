package org.fernandodev.ecommerce.wishlist.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.wishlist.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.wishlist.application.port.in.WishlistUseCases;
import org.fernandodev.ecommerce.wishlist.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.wishlist.application.port.out.InventoryPort;
import org.fernandodev.ecommerce.wishlist.application.port.out.WishlistRepository;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de wishlists.
 * <p>
 * Al añadir un item se valida de forma estricta contra Catalog e Inventory:
 * el producto debe existir y estar activo; si se indica variante, debe existir
 * en el producto y tener stock disponible.
 * </p>
 */
@RequiredArgsConstructor
public class WishlistService implements WishlistUseCases {

    private final WishlistRepository wishlists;
    private final CatalogPort catalog;
    private final InventoryPort inventory;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Wishlist create(UUID customerId, String name) {
        Wishlist wishlist = Wishlist.create(WishlistId.generate(), customerId, name, Instant.now());
        return wishlists.save(wishlist);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wishlist getById(WishlistId id) {
        return getExisting(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Wishlist> listByCustomer(UUID customerId) {
        return wishlists.findByCustomerId(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Wishlist rename(WishlistId id, String name) {
        Wishlist wishlist = getExisting(id);
        wishlist.rename(name, Instant.now());
        return wishlists.save(wishlist);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(WishlistId id) {
        getExisting(id);
        wishlists.delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Wishlist addItem(WishlistId id, AddItemCommand command) {
        Wishlist wishlist = getExisting(id);
        validateItem(command.productId(), command.variantId());
        wishlist.addItem(command.productId(), command.variantId(), Instant.now());
        return wishlists.save(wishlist);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Wishlist removeItem(WishlistId id, ProductId productId, ProductVariantId variantId) {
        Wishlist wishlist = getExisting(id);
        wishlist.removeItem(productId, variantId, Instant.now());
        return wishlists.save(wishlist);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Wishlist clear(WishlistId id) {
        Wishlist wishlist = getExisting(id);
        wishlist.clear(Instant.now());
        return wishlists.save(wishlist);
    }

    private void validateItem(ProductId productId, ProductVariantId variantId) {
        CatalogPort.ProductInfo product = catalog.findProduct(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));
        if (!product.active()) {
            throw new BusinessRuleViolationException("El producto no está disponible: " + productId);
        }
        if (variantId == null) {
            return;
        }
        if (!product.variantIds().contains(variantId)) {
            throw new BusinessRuleViolationException("La variante no existe: " + variantId);
        }
        if (inventory.availableStock(productId, variantId) <= 0) {
            throw new BusinessRuleViolationException("La variante no está disponible: " + variantId);
        }
    }

    private Wishlist getExisting(WishlistId id) {
        return wishlists.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist no encontrada: " + id));
    }
}
