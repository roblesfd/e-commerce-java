package org.fernandodev.ecommerce.cart.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.cart.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.cart.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.cart.application.dto.UpdateQuantityCommand;
import org.fernandodev.ecommerce.cart.application.port.in.CartUseCases;
import org.fernandodev.ecommerce.cart.application.port.out.AvailabilityProvider;
import org.fernandodev.ecommerce.cart.application.port.out.CartRepository;
import org.fernandodev.ecommerce.cart.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.cart.application.port.out.PriceProvider;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.cart.domain.value_object.AppliedCoupon;
import org.fernandodev.ecommerce.cart.domain.value_object.CartId;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItem;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItemId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Shopping Cart.
 * <p>
 * Orquesta el carrito y valida disponibilidad y precios a través de los
 * proveedores hacia los contextos Inventory y Pricing. No depende de Spring.
 * </p>
 */
@RequiredArgsConstructor
public class CartService implements CartUseCases {

    private final CartRepository carts;
    private final PriceProvider prices;
    private final AvailabilityProvider availability;
    private final CouponProvider coupons;

    @Override
    public Cart createCart(UUID ownerId) {
        return carts.findByOwner(ownerId)
                .orElseGet(() -> carts.save(Cart.create(CartId.generate(), ownerId, Instant.now())));
    }

    @Override
    public Cart getCart(UUID ownerId) {
        return createCart(ownerId);
    }

    @Override
    public Cart addItem(UUID ownerId, AddItemCommand command) {
        Cart cart = getCart(ownerId);
        assertAvailable(command.productId(), command.variantId(), command.quantity());
        Money unitPrice = prices.currentPrice(command.productId(), command.variantId());
        cart.addItem(command.productId(), command.variantId(), command.quantity(), unitPrice, Instant.now());
        return carts.save(cart);
    }

    @Override
    public Cart updateItemQuantity(UUID ownerId, CartItemId itemId, UpdateQuantityCommand command) {
        Cart cart = getExisting(ownerId);
        CartItem item = findItem(cart, itemId);
        assertAvailable(item.productId(), item.variantId(), command.quantity());
        cart.updateItemQuantity(itemId, command.quantity(), Instant.now());
        return carts.save(cart);
    }

    @Override
    public Cart removeItem(UUID ownerId, CartItemId itemId) {
        Cart cart = getExisting(ownerId);
        cart.removeItem(itemId);
        return carts.save(cart);
    }

    @Override
    public Cart clearCart(UUID ownerId) {
        Cart cart = getExisting(ownerId);
        cart.clear();
        return carts.save(cart);
    }

    @Override
    public Cart applyCoupon(UUID ownerId, ApplyCouponCommand command) {
        Cart cart = getExisting(ownerId);
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleViolationException("El carrito no puede estar vacío");
        }
        List<CouponItemDto> items = cart.getItems().stream()
                .map(item -> new CouponItemDto(item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        var discount = coupons.discountFor(ownerId, command.code(), items);
        cart.applyCoupon(new AppliedCoupon(command.code(), discount));
        return carts.save(cart);
    }

    @Override
    public Cart removeCoupon(UUID ownerId) {
        Cart cart = getExisting(ownerId);
        cart.removeCoupon();
        return carts.save(cart);
    }

    @Override
    public Optional<Money> subtotal(UUID ownerId) {
        return getExisting(ownerId).subtotal();
    }

    @Override
    public Optional<Money> discount(UUID ownerId) {
        return getExisting(ownerId).discount();
    }

    @Override
    public Optional<Money> total(UUID ownerId) {
        return getExisting(ownerId).total();
    }

    @Override
    public Cart validateCart(UUID ownerId) {
        Cart cart = getExisting(ownerId);
        for (CartItem item : List.copyOf(cart.getItems())) {
            assertAvailable(item.productId(), item.variantId(), item.quantity());
            cart.refreshPrice(item.id(), prices.currentPrice(item.productId(), item.variantId()), Instant.now());
        }
        return carts.save(cart);
    }

    @Override
    public void expireCart(UUID ownerId) {
        carts.delete(ownerId);
    }

    private Cart getExisting(UUID ownerId) {
        return carts.findByOwner(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));
    }

    private CartItem findItem(Cart cart, CartItemId itemId) {
        return cart.getItems().stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado en el carrito: " + itemId));
    }

    private void assertAvailable(ProductId productId, ProductVariantId variantId, int quantity) {
        if (!availability.available(productId, variantId, quantity)) {
            throw new BusinessRuleViolationException("Stock insuficiente para el producto: " + productId);
        }
    }
}