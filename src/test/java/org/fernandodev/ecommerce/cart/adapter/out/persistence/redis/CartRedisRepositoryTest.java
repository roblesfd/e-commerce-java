package org.fernandodev.ecommerce.cart.adapter.out.persistence.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.cart.application.port.out.CartRepository;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.cart.domain.value_object.AppliedCoupon;
import org.fernandodev.ecommerce.cart.domain.value_object.CartId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CartRedisRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private CartRepository repository;

    @Test
    void guardaYRecuperaCarrito() {
        UUID ownerId = UUID.randomUUID();
        Cart cart = Cart.create(CartId.generate(), ownerId, T0);
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);

        repository.save(cart);
        Cart loaded = repository.findByOwner(ownerId).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(cart.getId());
        assertThat(loaded.getOwnerId()).isEqualTo(ownerId);
        assertThat(loaded.getItems()).hasSize(1);
        assertThat(loaded.getItems().get(0).unitPrice()).isEqualTo(usd(900));
        assertThat(loaded.getItems().get(0).quantity()).isEqualTo(2);
        assertThat(loaded.subtotal()).contains(usd(1800));
    }

    @Test
    void findByOwnerInexistenteDevuelveVacio() {
        assertThat(repository.findByOwner(UUID.randomUUID())).isEmpty();
    }

    @Test
    void deleteEliminaElCarrito() {
        UUID ownerId = UUID.randomUUID();
        repository.save(Cart.create(CartId.generate(), ownerId, T0));

        repository.delete(ownerId);

        assertThat(repository.findByOwner(ownerId)).isEmpty();
    }

    @Test
    void saveSobrescribeElCarrito() {
        UUID ownerId = UUID.randomUUID();
        Cart cart = Cart.create(CartId.generate(), ownerId, T0);
        repository.save(cart);

        Cart updated = Cart.crear(cart.getId(), ownerId, java.util.List.of(), null, T0);
        repository.save(updated);

        Cart loaded = repository.findByOwner(ownerId).orElseThrow();
        assertThat(loaded.getId()).isEqualTo(cart.getId());
    }

    @Test
    void guardaYRecuperaCarritoConCupon() {
        UUID ownerId = UUID.randomUUID();
        Cart cart = Cart.create(CartId.generate(), ownerId, T0);
        cart.addItem(ProductId.generate(), null, 2, usd(900), T0);
        cart.applyCoupon(new AppliedCoupon("WELCOME10", usd(180)));

        repository.save(cart);
        Cart loaded = repository.findByOwner(ownerId).orElseThrow();

        assertThat(loaded.getCoupon()).isNotNull();
        assertThat(loaded.getCoupon().code()).isEqualTo("WELCOME10");
        assertThat(loaded.getCoupon().discount()).isEqualTo(usd(180));
        assertThat(loaded.total()).contains(usd(1620));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}