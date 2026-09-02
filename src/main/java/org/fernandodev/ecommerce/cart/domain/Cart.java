package org.fernandodev.ecommerce.cart.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.domain.value_object.AppliedCoupon;
import org.fernandodev.ecommerce.cart.domain.value_object.CartId;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItem;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItemId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Shopping Cart que representa el carrito de un
 * cliente (usuario IAM autenticado) antes del checkout.
 * <p>
 * Los precios de los items son snapshots del contexto Pricing al momento de
 * añadir el item. El carrito se persiste en Redis (dato temporal, no
 * autoritativo para órdenes) con TTL (spec §25).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Cart {

    private CartId id;
    private UUID ownerId;
    @Getter(AccessLevel.NONE)
    private List<CartItem> items;
    private AppliedCoupon coupon;
    private Instant updatedAt;

    /**
     * Crea un carrito vacío para un propietario.
     */
    public static Cart create(CartId id, UUID ownerId, Instant at) {
        return crear(id, ownerId, List.of(), null, at);
    }

    /**
     * Factoría completa con copia defensiva. Usada por la persistencia.
     */
    public static Cart crear(CartId id, UUID ownerId, List<CartItem> items, AppliedCoupon coupon, Instant updatedAt) {
        Cart cart = new Cart();
        cart.id = Objects.requireNonNull(id, "id no puede ser null");
        cart.ownerId = Objects.requireNonNull(ownerId, "ownerId no puede ser null");
        cart.items = new ArrayList<>(items == null ? List.of() : items);
        cart.coupon = coupon;
        cart.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return cart;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Añade o incrementa un item. Un item por (producto, variante); si ya
     * existe, la cantidad se incrementa y se actualiza el snapshot de precio.
     *
     * @return el item resultante.
     */
    public CartItem addItem(ProductId productId, ProductVariantId variantId, int quantity,
                            Money unitPrice, Instant at) {
        requirePositive(quantity);
        Objects.requireNonNull(unitPrice, "unitPrice no puede ser null");
        requireSameCurrency(unitPrice);

        int index = indexOf(productId, variantId);
        if (index >= 0) {
            CartItem existing = items.get(index);
            CartItem updated = new CartItem(
                    existing.id(), productId, variantId, existing.quantity() + quantity, unitPrice);
            items.set(index, updated);
            touch(at);
            return updated;
        }
        CartItem item = new CartItem(CartItemId.generate(), productId, variantId, quantity, unitPrice);
        items.add(item);
        touch(at);
        return item;
    }

    public void updateItemQuantity(CartItemId id, int quantity, Instant at) {
        requirePositive(quantity);
        int index = indexOf(id);
        CartItem existing = items.get(index);
        items.set(index, new CartItem(id, existing.productId(), existing.variantId(), quantity, existing.unitPrice()));
        touch(at);
    }

    /**
     * Actualiza el snapshot de precio unitario de un item (revalidación de
     * precios vigentes).
     */
    public void refreshPrice(CartItemId id, Money unitPrice, Instant at) {
        Objects.requireNonNull(unitPrice, "unitPrice no puede ser null");
        requireSameCurrency(unitPrice);
        int index = indexOf(id);
        CartItem existing = items.get(index);
        items.set(index, new CartItem(id, existing.productId(), existing.variantId(), existing.quantity(), unitPrice));
        touch(at);
    }

    public void removeItem(CartItemId id) {
        items.remove(indexOf(id));
    }

    public void clear() {
        items.clear();
    }

    /**
     * Aplica un cupón al carrito (reemplaza el anterior si existía).
     */
    public void applyCoupon(AppliedCoupon coupon) {
        this.coupon = Objects.requireNonNull(coupon, "coupon no puede ser null");
    }

    public void removeCoupon() {
        this.coupon = null;
    }

    /**
     * Subtotal del carrito (suma de unitPrice × cantidad).
     */
    public Optional<Money> subtotal() {
        return items.stream().map(CartItem::subtotal).reduce(Money::add);
    }

    /**
     * Descuento aplicado: el del cupón si existe, o cero.
     */
    public Optional<Money> discount() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        if (coupon != null) {
            return Optional.of(coupon.discount());
        }
        return Optional.of(Money.zero(items.get(0).unitPrice().currencyCode()));
    }

    public Optional<Money> total() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subtotal().orElseThrow().subtract(discount().orElseThrow()));
    }

    private void requireSameCurrency(Money unitPrice) {
        if (!items.isEmpty() && !unitPrice.currency().equals(items.get(0).unitPrice().currency())) {
            throw new BusinessRuleViolationException(
                    "La moneda debe coincidir con la del carrito: " + items.get(0).unitPrice().currencyCode());
        }
    }

    private int indexOf(ProductId productId, ProductVariantId variantId) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.productId().equals(productId) && Objects.equals(item.variantId(), variantId)) {
                return i;
            }
        }
        return -1;
    }

    private int indexOf(CartItemId id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new ResourceNotFoundException("Item no encontrado en el carrito: " + id);
    }

    private void touch(Instant at) {
        this.updatedAt = at;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("La cantidad debe ser positiva");
        }
    }
}