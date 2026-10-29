package org.fernandodev.ecommerce.cart.domain;

import java.time.Instant;
import java.util.*;

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
    private Map<CartItemId, CartItem> items;
    private AppliedCoupon coupon;
    private Instant updatedAt;

    /**
     * Crea un carrito vacío para un propietario.
     *
     * @param id      identificador del carrito.
     * @param ownerId identificador del propietario (usuario IAM).
     * @param at      momento de creación.
     * @return el carrito vacío.
     */
    public static Cart create(CartId id, UUID ownerId, Instant at) {
        return crear(id, ownerId, Map.of(), null, at);
    }

    /**
     * Factoría completa con copia defensiva de los items. Usada por la
     * persistencia para reconstruir el agregado.
     *
     * @param id        identificador del carrito.
     * @param ownerId   identificador del propietario (usuario IAM).
     * @param items     items del carrito.
     * @param coupon    cupón aplicado (null si no hay).
     * @param updatedAt momento de la última modificación.
     * @return el carrito reconstruido.
     * @throws NullPointerException si {@code id}, {@code ownerId} o {@code updatedAt} son {@code null}.
     */
    public static Cart crear(CartId id, UUID ownerId, Map<CartItemId, CartItem> items, AppliedCoupon coupon, Instant updatedAt) {
        Cart cart = new Cart();
        cart.id = Objects.requireNonNull(id, "id no puede ser null");
        cart.ownerId = Objects.requireNonNull(ownerId, "ownerId no puede ser null");
        cart.items = new HashMap<>(items == null ? Map.of() : items);
        cart.coupon = coupon;
        cart.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt no puede ser null");
        return cart;
    }

    /**
     * @return los items del carrito como map inmutable.
     */
    public Map<CartItemId, CartItem> getItems() {
        return Collections.unmodifiableMap(items);
    }

    /**
     * Añade o incrementa un item. Un item por (producto, variante); si ya
     * existe, la cantidad se incrementa y se actualiza el snapshot de precio.
     *
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad a añadir.
     * @param unitPrice precio unitario capturado.
     * @param at        momento de la operación.
     * @return el item resultante.
     * @throws BusinessRuleViolationException si {@code quantity} no es positiva o la moneda no coincide con el carrito.
     * @throws NullPointerException           si {@code unitPrice} es {@code null}.
     */
    public CartItem addItem(ProductId productId, ProductVariantId variantId, int quantity,
                            Money unitPrice, Instant at) {
        requirePositive(quantity);
        Objects.requireNonNull(unitPrice, "unitPrice no puede ser null");
        requireSameCurrency(unitPrice);

        CartItem existing = items.values().stream().filter(item ->
                item.productId().equals(productId) &&
                item.variantId().equals(variantId)).findFirst().orElse(null);

        if (existing != null) {
            CartItem updated = new CartItem(
                    existing.id(), productId, variantId, existing.quantity() + quantity, unitPrice);
            items.put(existing.id(), updated);
            setUpdatedAt(at);
            return updated;
        }
        CartItem item = new CartItem(CartItemId.generate(), productId, variantId, quantity, unitPrice);
        items.put(item.id(), item);
        setUpdatedAt(at);
        return item;
    }

    /**
     * Actualiza la cantidad de un item existente.
     *
     * @param id       identificador del item.
     * @param quantity nueva cantidad.
     * @param at       momento de la operación.
     * @throws BusinessRuleViolationException si {@code quantity} no es positiva.
     * @throws ResourceNotFoundException      si el item no pertenece al carrito.
     */
    public void updateItemQuantity(CartItemId id, int quantity, Instant at) {
        requirePositive(quantity);
        CartItem existing = findItem(id);
        items.put(id, new CartItem(
                id,
                existing.productId(),
                existing.variantId(),
                quantity,
                existing.unitPrice()
        ));
        setUpdatedAt(at);
    }

    /**
     * Actualiza el snapshot de precio unitario de un item (revalidación de
     * precios vigentes).
     *
     * @param id        identificador del item.
     * @param unitPrice nuevo precio unitario.
     * @param at        momento de la operación.
     * @throws BusinessRuleViolationException si la moneda no coincide con el carrito.
     * @throws NullPointerException           si {@code unitPrice} es {@code null}.
     * @throws ResourceNotFoundException      si el item no pertenece al carrito.
     */
    public void refreshPrice(CartItemId id, Money unitPrice, Instant at) {
        Objects.requireNonNull(unitPrice, "unitPrice no puede ser null");
        requireSameCurrency(unitPrice);
        items.compute(id, (k, existing) -> new CartItem(
                id,
                existing.productId(),
                existing.variantId(),
                existing.quantity(),
                unitPrice
        ));
        setUpdatedAt(at);
    }

    /**
     * Elimina un item del carrito.
     *
     * @param id identificador del item.
     * @throws ResourceNotFoundException si el item no pertenece al carrito.
     */
    public void removeItem(CartItemId id) {
        findItem(id);
        items.remove(id);
    }

    /**
     * Vacía el carrito (elimina todos los items).
     */
    public void clear() {
        items.clear();
    }

    /**
     * Aplica un cupón al carrito (reemplaza el anterior si existía).
     *
     * @param coupon cupón a aplicar.
     * @throws NullPointerException si {@code coupon} es {@code null}.
     */
    public void applyCoupon(AppliedCoupon coupon) {
        this.coupon = Objects.requireNonNull(coupon, "coupon no puede ser null");
    }

    /**
     * Elimina el cupón aplicado.
     */
    public void removeCoupon() {
        this.coupon = null;
    }

    /**
     * @return el subtotal del carrito (suma de {@code unitPrice × cantidad}),
     *         o {@code Optional.empty()} si el carrito está vacío.
     */
    public Optional<Money> subtotal() {
        return items.values().stream().map(CartItem::subtotal).reduce(Money::add);
    }

    /**
     * @return el descuento aplicado: el del cupón si existe, o cero; o
     *         {@code Optional.empty()} si el carrito está vacío.
     */
    public Optional<Money> discount() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        if (coupon != null) {
            return Optional.of(coupon.discount());
        }
        return Optional.of(Money.zero(
                items.values()
                    .iterator()
                    .next()
                    .unitPrice()
                    .currencyCode())
        );
    }

    /**
     * @return el total del carrito ({@code subtotal − descuento}), o
     *         {@code Optional.empty()} si el carrito está vacío.
     */
    public Optional<Money> total() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subtotal().orElseThrow().subtract(discount().orElseThrow())
        );
    }

    /**
     * Valida que la moneda del precio coincida con la del carrito.
     *
     * @param unitPrice precio a validar.
     * @throws BusinessRuleViolationException si la moneda difiere de la del carrito.
     */
    private void requireSameCurrency(Money unitPrice) {
        if (!items.isEmpty()
                && !unitPrice.currency().equals(items.values().iterator().next().unitPrice().currency())) {
            throw new BusinessRuleViolationException(
                    "La moneda debe coincidir con la del carrito: "
                            + items.values().iterator().next().unitPrice().currencyCode()
            );
        }
    }
    private CartItem findItem(CartItemId id) {
        CartItem item = items.get(id);

        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado en el carrito: " + id);
        }

        return item;
    }

    /**
     * Actualiza la fecha de última modificación.
     *
     * @param at momento de la operación.
     */
    private void setUpdatedAt(Instant at) {
        this.updatedAt = at;
    }

    /**
     * Valida que la cantidad sea positiva.
     *
     * @param quantity cantidad a validar.
     * @throws BusinessRuleViolationException si {@code quantity} es menor o igual a cero.
     */
    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("La cantidad debe ser positiva");
        }
    }
}
