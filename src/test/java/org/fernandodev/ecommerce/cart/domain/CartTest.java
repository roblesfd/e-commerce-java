package org.fernandodev.ecommerce.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
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
import org.junit.jupiter.api.Test;

class CartTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T1 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    void createCreaCarritoVacio() {
        Cart cart = carrito();

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.subtotal()).isEmpty();
        assertThat(cart.total()).isEmpty();
    }

    @Test
    void addItemAgregaItemConPrecioSnapshot() {
        Cart cart = carrito();
        ProductId productId = ProductId.generate();

        CartItem item = cart.addItem(productId, null, 2, usd(900), T0);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.unitPrice()).isEqualTo(usd(900));
        assertThat(cart.subtotal()).contains(usd(1800));
    }

    @Test
    void addItemMismoProductoIncrementaCantidadYActualizaPrecio() {
        Cart cart = carrito();
        ProductId productId = ProductId.generate();
        cart.addItem(productId, null, 2, usd(900), T0);

        CartItem updated = cart.addItem(productId, null, 3, usd(950), T1);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(updated.quantity()).isEqualTo(5);
        assertThat(updated.unitPrice()).isEqualTo(usd(950));
    }

    @Test
    void addItemConCantidadInvalidaLanzaExcepcion() {
        Cart cart = carrito();

        assertThatThrownBy(() -> cart.addItem(ProductId.generate(), null, 0, usd(900), T0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addItemConDivisaDistintaLanzaExcepcion() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 1, usd(900), T0);

        assertThatThrownBy(() -> cart.addItem(ProductId.generate(), null, 1, Money.of("900", "MXN"), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateItemQuantityCambiaLaCantidad() {
        Cart cart = carrito();
        CartItem item = cart.addItem(ProductId.generate(), null, 2, usd(900), T0);

        cart.updateItemQuantity(item.id(), 4, T1);

        assertThat(cart.getItems().get(0).quantity()).isEqualTo(4);
        assertThat(cart.subtotal()).contains(usd(3600));
    }

    @Test
    void updateItemQuantityInexistenteLanzaExcepcion() {
        Cart cart = carrito();

        assertThatThrownBy(() -> cart.updateItemQuantity(CartItemId.generate(), 2, T0))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemQuantityConCeroLanzaExcepcion() {
        Cart cart = carrito();
        CartItem item = cart.addItem(ProductId.generate(), null, 2, usd(900), T0);

        assertThatThrownBy(() -> cart.updateItemQuantity(item.id(), 0, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void removeItemQuitaElItem() {
        Cart cart = carrito();
        CartItem item = cart.addItem(ProductId.generate(), null, 2, usd(900), T0);

        cart.removeItem(item.id());

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void removeItemInexistenteLanzaExcepcion() {
        Cart cart = carrito();

        assertThatThrownBy(() -> cart.removeItem(CartItemId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void clearVaciaElCarrito() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);
        cart.addItem(ProductId.generate(), null, 1, usd(500), T0);

        cart.clear();

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void subtotalSumaUnitPricePorCantidad() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);
        cart.addItem(ProductId.generate(), null, 1, usd(500), T0);

        assertThat(cart.subtotal()).contains(usd(2300));
    }

    @Test
    void totalEsSubtotalMenosDescuento() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);

        assertThat(cart.discount()).contains(usd(0));
        assertThat(cart.total()).contains(usd(1800));
    }

    @Test
    void itemUnicoPorProductoYVariante() {
        Cart cart = carrito();
        ProductId productId = ProductId.generate();
        ProductVariantId variantId = ProductVariantId.generate();
        cart.addItem(productId, variantId, 1, usd(900), T0);
        cart.addItem(productId, null, 1, usd(900), T0);

        assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    void applyCouponAplicaDescuentoYTotal() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);

        cart.applyCoupon(new AppliedCoupon("WELCOME10", usd(180)));

        assertThat(cart.getCoupon().code()).isEqualTo("WELCOME10");
        assertThat(cart.discount()).contains(usd(180));
        assertThat(cart.total()).contains(usd(1620));
    }

    @Test
    void removeCouponLimpiaElDescuento() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);
        cart.applyCoupon(new AppliedCoupon("WELCOME10", usd(180)));

        cart.removeCoupon();

        assertThat(cart.getCoupon()).isNull();
        assertThat(cart.discount()).contains(usd(0));
        assertThat(cart.total()).contains(usd(1800));
    }

    @Test
    void applyCouponReemplazaElAnterior() {
        Cart cart = carrito();
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);
        cart.applyCoupon(new AppliedCoupon("WELCOME10", usd(180)));

        cart.applyCoupon(new AppliedCoupon("OTRO", usd(360)));

        assertThat(cart.getCoupon().code()).isEqualTo("OTRO");
        assertThat(cart.discount()).contains(usd(360));
    }

    private static Cart carrito() {
        return Cart.create(CartId.generate(), UUID.randomUUID(), T0);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}