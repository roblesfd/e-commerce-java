package org.fernandodev.ecommerce.wishlist.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una wishlist de un cliente.
 * <p>
 * Soporta múltiples wishlists por cliente, referenciando productos o variantes
 * del catálogo sin duplicar su información. Previene items duplicados por
 * (producto, variante).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Wishlist {

    private WishlistId id;
    private UUID customerId;
    private String name;
    @Getter(AccessLevel.NONE)
    private List<WishlistItem> items;
    private Instant createdAt;
    private Instant updatedAt;

    public static Wishlist create(WishlistId id, UUID customerId, String name, Instant at) {
        Wishlist wishlist = new Wishlist();
        wishlist.id = Objects.requireNonNull(id, "id no puede ser null");
        wishlist.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        wishlist.name = validateName(name);
        wishlist.items = new ArrayList<>();
        wishlist.createdAt = Objects.requireNonNull(at, "at no puede ser null");
        wishlist.updatedAt = at;
        return wishlist;
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Wishlist crear(
            WishlistId id,
            UUID customerId,
            String name,
            List<WishlistItem> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        Wishlist wishlist = new Wishlist();
        wishlist.id = Objects.requireNonNull(id, "id no puede ser null");
        wishlist.customerId = Objects.requireNonNull(customerId, "customerId no puede ser null");
        wishlist.name = validateName(name);
        wishlist.items = new ArrayList<>(items == null ? List.of() : items);
        wishlist.createdAt = Objects.requireNonNull(createdAt, "createdAt no puede ser null");
        wishlist.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return wishlist;
    }

    public List<WishlistItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void rename(String name, Instant at) {
        this.name = validateName(name);
        this.updatedAt = at;
    }

    public void addItem(ProductId productId, ProductVariantId variantId, Instant at) {
        boolean exists = items.stream().anyMatch(item ->
                item.productId().equals(productId)
                        && Objects.equals(item.variantId(), variantId));
        if (exists) {
            throw new BusinessRuleViolationException("El item ya está en la wishlist");
        }
        items.add(new WishlistItem(productId, variantId, at));
        this.updatedAt = at;
    }

    public void removeItem(ProductId productId, ProductVariantId variantId, Instant at) {
        boolean removed = items.removeIf(item ->
                item.productId().equals(productId)
                        && Objects.equals(item.variantId(), variantId));
        if (!removed) {
            throw new BusinessRuleViolationException("El item no está en la wishlist");
        }
        this.updatedAt = at;
    }

    public void clear(Instant at) {
        items.clear();
        this.updatedAt = at;
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "name no puede ser null");
        if (name.isBlank()) {
            throw new BusinessRuleViolationException("El nombre de la wishlist no puede estar vacío");
        }
        return name.trim();
    }
}