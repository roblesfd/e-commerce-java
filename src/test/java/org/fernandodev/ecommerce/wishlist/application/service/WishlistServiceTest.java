package org.fernandodev.ecommerce.wishlist.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.wishlist.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.wishlist.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.wishlist.application.port.out.InventoryPort;
import org.fernandodev.ecommerce.wishlist.application.port.out.WishlistRepository;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.junit.jupiter.api.Test;

class WishlistServiceTest {

    private final FakeWishlistRepository repository = new FakeWishlistRepository();
    private final FakeCatalogPort catalog = new FakeCatalogPort();
    private final FakeInventoryPort inventory = new FakeInventoryPort();
    private final WishlistService service = new WishlistService(repository, catalog, inventory);

    @Test
    void createGuardaWishlistDelCliente() {
        UUID customerId = UUID.randomUUID();

        Wishlist wishlist = service.create(customerId, "Mi lista");

        assertThat(wishlist.getName()).isEqualTo("Mi lista");
        assertThat(repository.findById(wishlist.getId())).contains(wishlist);
    }

    @Test
    void listByCustomerDevuelveSoloLasDelCliente() {
        UUID customerId = UUID.randomUUID();
        service.create(customerId, "Lista 1");
        service.create(UUID.randomUUID(), "Otra");

        assertThat(service.listByCustomer(customerId)).hasSize(1);
    }

    @Test
    void renameCambiaElNombre() {
        Wishlist wishlist = service.create(UUID.randomUUID(), "Lista");

        Wishlist renamed = service.rename(wishlist.getId(), "Favoritos");

        assertThat(renamed.getName()).isEqualTo("Favoritos");
    }

    @Test
    void deleteEliminaLaWishlist() {
        Wishlist wishlist = service.create(UUID.randomUUID(), "Lista");

        service.delete(wishlist.getId());

        assertThat(repository.findById(wishlist.getId())).isEmpty();
    }

    @Test
    void addItemValidaProductoYVariante() {
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = service.create(customerId, "Lista");
        ProductId product = ProductId.generate();
        ProductVariantId variant = ProductVariantId.generate();
        catalog.product = Optional.of(new CatalogPort.ProductInfo(product, true, Set.of(variant)));
        inventory.stock = 5;

        Wishlist updated = service.addItem(wishlist.getId(),
                new AddItemCommand(product, variant));

        assertThat(updated.getItems()).hasSize(1);
    }

    @Test
    void addItemDeProductoInactivoLanzaViolacion() {
        Wishlist wishlist = service.create(UUID.randomUUID(), "Lista");
        ProductId product = ProductId.generate();
        catalog.product = Optional.of(new CatalogPort.ProductInfo(product, false, Set.of()));

        assertThatThrownBy(() -> service.addItem(wishlist.getId(),
                new AddItemCommand(product, null)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addItemDeVarianteSinStockLanzaViolacion() {
        Wishlist wishlist = service.create(UUID.randomUUID(), "Lista");
        ProductId product = ProductId.generate();
        ProductVariantId variant = ProductVariantId.generate();
        catalog.product = Optional.of(new CatalogPort.ProductInfo(product, true, Set.of(variant)));
        inventory.stock = 0;

        assertThatThrownBy(() -> service.addItem(wishlist.getId(),
                new AddItemCommand(product, variant)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void addItemDeProductoInexistenteLanzaNotFound() {
        Wishlist wishlist = service.create(UUID.randomUUID(), "Lista");
        catalog.product = Optional.empty();

        assertThatThrownBy(() -> service.addItem(wishlist.getId(),
                new AddItemCommand(ProductId.generate(), null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItemYClearFuncionan() {
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = service.create(customerId, "Lista");
        ProductId product = ProductId.generate();
        catalog.product = Optional.of(new CatalogPort.ProductInfo(product, true, Set.of()));
        wishlist = service.addItem(wishlist.getId(), new AddItemCommand(product, null));
        assertThat(wishlist.getItems()).hasSize(1);

        Wishlist cleared = service.clear(wishlist.getId());
        assertThat(cleared.getItems()).isEmpty();

        wishlist = service.addItem(wishlist.getId(), new AddItemCommand(product, null));
        Wishlist removed = service.removeItem(wishlist.getId(), product, null);
        assertThat(removed.getItems()).isEmpty();
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(WishlistId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static final class FakeWishlistRepository implements WishlistRepository {

        private final Map<WishlistId, Wishlist> byId = new HashMap<>();

        @Override
        public Wishlist save(Wishlist wishlist) {
            byId.put(wishlist.getId(), wishlist);
            return wishlist;
        }

        @Override
        public Optional<Wishlist> findById(WishlistId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Wishlist> findByCustomerId(UUID customerId) {
            return byId.values().stream().filter(w -> w.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public void delete(WishlistId id) {
            byId.remove(id);
        }
    }

    private static final class FakeCatalogPort implements CatalogPort {
        private Optional<ProductInfo> product = Optional.empty();

        @Override
        public Optional<ProductInfo> findProduct(ProductId productId) {
            return product;
        }
    }

    private static final class FakeInventoryPort implements InventoryPort {
        private int stock = 0;

        @Override
        public int availableStock(ProductId productId, ProductVariantId variantId) {
            return stock;
        }
    }
}