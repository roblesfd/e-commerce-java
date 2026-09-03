package org.fernandodev.ecommerce.wishlist.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.junit.jupiter.api.Test;

class WishlistTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaWishlistVacia() {
        Wishlist wishlist = wishlist("Mi lista");

        assertThat(wishlist.getItems()).isEmpty();
        assertThat(wishlist.getName()).isEqualTo("Mi lista");
    }

    @Test
    void createConNombreVacioLanzaExcepcion() {
        assertThatThrownBy(() -> Wishlist.create(WishlistId.generate(), UUID.randomUUID(), "  ", T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addItemAnadeYSigueElMismoProductoConDistintaVariante() {
        Wishlist wishlist = wishlist("Lista");
        ProductId product = ProductId.generate();
        ProductVariantId v1 = ProductVariantId.generate();
        ProductVariantId v2 = ProductVariantId.generate();

        wishlist.addItem(product, v1, T1);
        wishlist.addItem(product, v2, T1);

        assertThat(wishlist.getItems()).hasSize(2);
        assertThat(wishlist.getItems().get(0).addedAt()).isEqualTo(T1);
    }

    @Test
    void addItemDuplicadoLanzaExcepcion() {
        Wishlist wishlist = wishlist("Lista");
        ProductId product = ProductId.generate();
        ProductVariantId variant = ProductVariantId.generate();

        wishlist.addItem(product, variant, T1);

        assertThatThrownBy(() -> wishlist.addItem(product, variant, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addItemSinVarianteDuplicadoConVarianteNoEsDuplicado() {
        Wishlist wishlist = wishlist("Lista");
        ProductId product = ProductId.generate();

        wishlist.addItem(product, null, T1);
        wishlist.addItem(product, ProductVariantId.generate(), T1);

        assertThat(wishlist.getItems()).hasSize(2);
    }

    @Test
    void removeItemEliminaElAdecuado() {
        Wishlist wishlist = wishlist("Lista");
        ProductId product = ProductId.generate();
        wishlist.addItem(product, null, T1);
        wishlist.addItem(ProductId.generate(), null, T1);

        wishlist.removeItem(product, null, T1);

        assertThat(wishlist.getItems()).hasSize(1);
    }

    @Test
    void removeItemInexistenteLanzaExcepcion() {
        Wishlist wishlist = wishlist("Lista");

        assertThatThrownBy(() -> wishlist.removeItem(ProductId.generate(), null, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void clearVaciaLaWishlist() {
        Wishlist wishlist = wishlist("Lista");
        wishlist.addItem(ProductId.generate(), null, T1);
        wishlist.addItem(ProductId.generate(), null, T1);

        wishlist.clear(T1);

        assertThat(wishlist.getItems()).isEmpty();
    }

    @Test
    void renameCambiaElNombre() {
        Wishlist wishlist = wishlist("Lista");

        wishlist.rename("Favoritos", T1);

        assertThat(wishlist.getName()).isEqualTo("Favoritos");
    }

    private static Wishlist wishlist(String name) {
        return Wishlist.create(WishlistId.generate(), UUID.randomUUID(), name, T1);
    }
}