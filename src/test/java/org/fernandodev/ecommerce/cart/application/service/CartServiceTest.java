package org.fernandodev.ecommerce.cart.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.cart.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.cart.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.cart.application.dto.UpdateQuantityCommand;
import org.fernandodev.ecommerce.cart.application.port.out.AvailabilityProvider;
import org.fernandodev.ecommerce.cart.application.port.out.CartRepository;
import org.fernandodev.ecommerce.cart.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.cart.application.port.out.PriceProvider;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartServiceTest {

    private final FakeCartRepository repository = new FakeCartRepository();
    private FakePriceProvider prices;
    private FakeAvailabilityProvider availability;
    private FakeCouponProvider coupons;
    private CartService service;

    @BeforeEach
    void setUp() {
        prices = new FakePriceProvider();
        availability = new FakeAvailabilityProvider();
        coupons = new FakeCouponProvider();
        service = new CartService(repository, prices, availability, coupons);
    }

    @Test
    void createCartDevuelveCarritoVacio() {
        Cart cart = service.createCart(owner());

        assertThat(cart.getItems()).isEmpty();
        assertThat(repository.findByOwner(cart.getOwnerId())).contains(cart);
    }

    @Test
    void getCartCreaSiNoExiste() {
        UUID ownerId = owner();

        Cart cart = service.getCart(ownerId);

        assertThat(cart.getItems()).isEmpty();
        assertThat(repository.findByOwner(ownerId)).contains(cart);
    }

    @Test
    void addItemValidaDisponibilidadYSnapshotDePrecio() {
        UUID ownerId = owner();
        ProductId productId = ProductId.generate();
        prices.price = usd(900);

        Cart cart = service.addItem(ownerId, new AddItemCommand(productId, null, 2));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().values().stream().findFirst().get().unitPrice()).isEqualTo(usd(900));
        assertThat(cart.getItems().values().stream().findFirst().get().quantity()).isEqualTo(2);
    }

    @Test
    void addItemSinStockLanzaViolacion() {
        UUID ownerId = owner();
        availability.available = false;

        assertThatThrownBy(() -> service.addItem(ownerId, new AddItemCommand(ProductId.generate(), null, 2)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateItemQuantityValidaDisponibilidad() {
        UUID ownerId = owner();
        ProductId productId = ProductId.generate();
        prices.price = usd(900);
        Cart cart = service.addItem(ownerId, new AddItemCommand(productId, null, 2));

        Cart updated = service.updateItemQuantity(ownerId,  cart.getItems().values().stream().findFirst().get().id(), new UpdateQuantityCommand(5));

        assertThat(updated.getItems().values().stream().findFirst().get().quantity()).isEqualTo(5);
    }

    @Test
    void removeItemEliminaElItem() {
        UUID ownerId = owner();
        prices.price = usd(900);
        Cart cart = service.addItem(ownerId, new AddItemCommand(ProductId.generate(), null, 1));

        Cart updated = service.removeItem(ownerId, cart.getItems().values().stream().findFirst().get().id());

        assertThat(updated.getItems()).isEmpty();
    }

    @Test
    void clearCartVaciaElCarrito() {
        UUID ownerId = owner();
        prices.price = usd(900);
        service.addItem(ownerId, new AddItemCommand(ProductId.generate(), null, 1));

        Cart cleared = service.clearCart(ownerId);

        assertThat(cleared.getItems()).isEmpty();
    }

    @Test
    void subtotalYTotalCalculan() {
        UUID ownerId = owner();
        ProductId a = ProductId.generate();
        ProductId b = ProductId.generate();
        prices.price = usd(900);
        service.addItem(ownerId, new AddItemCommand(a, null, 2));
        prices.price = usd(500);
        service.addItem(ownerId, new AddItemCommand(b, null, 1));

        assertThat(service.subtotal(ownerId)).contains(usd(2300));
        assertThat(service.total(ownerId)).contains(usd(2300));
    }

    @Test
    void validateCartActualizaSnapshotsDePrecio() {
        UUID ownerId = owner();
        ProductId productId = ProductId.generate();
        prices.price = usd(900);
        Cart cart = service.addItem(ownerId, new AddItemCommand(productId, null, 1));

        prices.price = usd(1000);
        Cart validated = service.validateCart(ownerId);

        assertThat(validated.getItems()
                .values()
                .stream()
                .filter(i -> i.productId().equals(productId)
                ).findFirst()
                .get()
                .unitPrice()
        ).isEqualTo(usd(1000));
    }

    @Test
    void validateCartConItemSinStockLanzaViolacion() {
        UUID ownerId = owner();
        prices.price = usd(900);
        service.addItem(ownerId, new AddItemCommand(ProductId.generate(), null, 1));
        availability.available = false;

        assertThatThrownBy(() -> service.validateCart(ownerId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void expireCartEliminaElCarrito() {
        UUID ownerId = owner();
        service.createCart(ownerId);

        service.expireCart(ownerId);

        assertThat(repository.findByOwner(ownerId)).isEmpty();
    }

    @Test
    void applyCouponAplicaElDescuentoAlCarrito() {
        UUID ownerId = owner();
        prices.price = usd(900);
        service.addItem(ownerId, new AddItemCommand(ProductId.generate(), null, 2));
        coupons.discount = usd(180);

        Cart cart = service.applyCoupon(ownerId, new ApplyCouponCommand("WELCOME10"));

        assertThat(cart.getCoupon().code()).isEqualTo("WELCOME10");
        assertThat(cart.getCoupon().discount()).isEqualTo(usd(180));
        assertThat(service.total(ownerId)).contains(usd(1620));
    }

    @Test
    void applyCouponConCarritoVacioLanzaViolacion() {
        UUID ownerId = owner();
        service.createCart(ownerId);

        assertThatThrownBy(() -> service.applyCoupon(ownerId, new ApplyCouponCommand("WELCOME10")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void removeCouponLimpiaElCupon() {
        UUID ownerId = owner();
        prices.price = usd(900);
        service.addItem(ownerId, new AddItemCommand(ProductId.generate(), null, 2));
        coupons.discount = usd(180);
        service.applyCoupon(ownerId, new ApplyCouponCommand("WELCOME10"));

        Cart cart = service.removeCoupon(ownerId);

        assertThat(cart.getCoupon()).isNull();
        assertThat(service.total(ownerId)).contains(usd(1800));
    }

    @Test
    void operarSobreCarritoInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.total(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static UUID owner() {
        return UUID.randomUUID();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeCartRepository implements CartRepository {

        private final Map<UUID, Cart> byOwner = new HashMap<>();

        @Override
        public Cart save(Cart cart) {
            byOwner.put(cart.getOwnerId(), cart);
            return cart;
        }

        @Override
        public Optional<Cart> findByOwner(UUID ownerId) {
            return Optional.ofNullable(byOwner.get(ownerId));
        }

        @Override
        public void delete(UUID ownerId) {
            byOwner.remove(ownerId);
        }
    }

    private static final class FakePriceProvider implements PriceProvider {

        private Money price = usd(900);

        @Override
        public Money currentPrice(ProductId productId, ProductVariantId variantId) {
            return price;
        }
    }

    private static final class FakeAvailabilityProvider implements AvailabilityProvider {

        private boolean available = true;

        @Override
        public boolean available(ProductId productId, ProductVariantId variantId, int quantity) {
            return available;
        }
    }

    private static final class FakeCouponProvider implements CouponProvider {

        private Money discount = usd(100);

        @Override
        public Money discountFor(UUID customerId, String code, java.util.List<CouponItemDto> items) {
            return discount;
        }
    }
}
