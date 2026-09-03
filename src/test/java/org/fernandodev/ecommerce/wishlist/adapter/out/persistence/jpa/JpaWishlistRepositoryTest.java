package org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.wishlist.application.port.out.WishlistRepository;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaWishlistRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private WishlistRepository repository;

    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void guardaYRecuperaWishlistConItems() {
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = Wishlist.create(WishlistId.generate(), customerId, "Mi lista", AT);
        ProductId product = ProductId.generate();
        wishlist.addItem(product, ProductVariantId.generate(), AT);
        wishlist.addItem(ProductId.generate(), null, AT);

        repository.save(wishlist);
        Wishlist loaded = repository.findById(wishlist.getId()).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Mi lista");
        assertThat(loaded.getItems()).hasSize(2);
        assertThat(loaded.getItems().get(0).productId()).isEqualTo(product);
    }

    @Test
    void findByCustomerIdDevuelveSoloLasDelCliente() {
        UUID customerId = UUID.randomUUID();
        repository.save(Wishlist.create(WishlistId.generate(), customerId, "Lista 1", AT));
        repository.save(Wishlist.create(WishlistId.generate(), UUID.randomUUID(), "Otra", AT));

        assertThat(repository.findByCustomerId(customerId)).hasSize(1);
    }

    @Test
    void deleteEliminaLaWishlist() {
        Wishlist wishlist = Wishlist.create(WishlistId.generate(), UUID.randomUUID(), "Lista", AT);
        repository.save(wishlist);

        repository.delete(wishlist.getId());

        assertThat(repository.findById(wishlist.getId())).isEmpty();
    }
}