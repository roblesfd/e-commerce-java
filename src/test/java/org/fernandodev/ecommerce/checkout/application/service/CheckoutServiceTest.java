package org.fernandodev.ecommerce.checkout.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.application.dto.CartData;
import org.fernandodev.ecommerce.checkout.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.checkout.application.dto.OrderData;
import org.fernandodev.ecommerce.checkout.application.dto.ProductInfo;
import org.fernandodev.ecommerce.checkout.application.dto.StartCheckoutCommand;
import org.fernandodev.ecommerce.checkout.application.port.out.CartProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.CheckoutRepository;
import org.fernandodev.ecommerce.checkout.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.CustomerProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.InventoryReservationPort;
import org.fernandodev.ecommerce.checkout.application.port.out.OrderPort;
import org.fernandodev.ecommerce.checkout.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.checkout.application.port.out.PriceProvider;
import org.fernandodev.ecommerce.checkout.application.port.out.ProductProvider;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutStatus;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutServiceTest {

    private final FakeCheckoutRepository repository = new FakeCheckoutRepository();
    private final FakeCartProvider carts = new FakeCartProvider();
    private final FakeProductProvider products = new FakeProductProvider();
    private final FakePriceProvider prices = new FakePriceProvider();
    private final FakeInventoryReservationPort inventory = new FakeInventoryReservationPort();
    private final FakeCustomerProvider customers = new FakeCustomerProvider();
    private final FakeCouponProvider coupons = new FakeCouponProvider();
    private final FakeOrderPort orders = new FakeOrderPort();
    private final FakePaymentPort payments = new FakePaymentPort();
    private final CheckoutService service = new CheckoutService(
            repository, carts, products, prices, inventory, customers, coupons, orders, payments);

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        carts.cart = new CartData(UUID.randomUUID(), List.of(
                new CartData.CartItemData(ProductId.generate(), null, 2, usd(900))), null);
    }

    @Test
    void startCreaCheckoutConReservasYTotales() {
        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));

        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.PENDING);
        assertThat(checkout.getSubtotal()).isEqualTo(usd(1800));
        assertThat(checkout.getReservations()).hasSize(1);
        assertThat(repository.findById(checkout.getId())).contains(checkout);
    }

    @Test
    void startIdempotenteDevuelveElExistenteSinReReservar() {
        Checkout first = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));
        int reservesBefore = inventory.reserveCount;

        Checkout second = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(inventory.reserveCount).isEqualTo(reservesBefore);
    }

    @Test
    void startConCarritoVacioLanzaViolacion() {
        carts.cart = new CartData(UUID.randomUUID(), List.of(), null);

        assertThatThrownBy(() -> service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID())))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void startConDireccionInexistenteLanzaNotFound() {
        customers.fail = true;

        assertThatThrownBy(() -> service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(inventory.releaseCount).isEqualTo(0);
    }

    @Test
    void startConCuponAplicaDescuento() {
        carts.cart = new CartData(carts.cart.cartId(), carts.cart.items(),
                new CartData.AppliedCouponData("WELCOME10", usd(180)));
        coupons.discount = usd(180);

        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));

        assertThat(checkout.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(checkout.getDiscounts()).isEqualTo(usd(180));
        assertThat(checkout.getTotal()).isEqualTo(usd(1620));
    }

    @Test
    void confirmCreaOrdenYPagoYConfirma() {
        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));
        payments.approved = true;

        Checkout confirmed = service.confirm(customerId, checkout.getId());

        assertThat(confirmed.getStatus()).isEqualTo(CheckoutStatus.CONFIRMED);
        assertThat(confirmed.getOrderId()).isNotNull();
        assertThat(confirmed.getPaymentId()).isNotNull();
        assertThat(orders.confirmed).contains(confirmed.getOrderId());
    }

    @Test
    void confirmConPagoFallidoCancelaOrdenYLiberaReservas() {
        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));
        payments.approved = false;

        Checkout failed = service.confirm(customerId, checkout.getId());

        assertThat(failed.getStatus()).isEqualTo(CheckoutStatus.FAILED);
        assertThat(orders.cancelled).contains(failed.getOrderId());
        assertThat(inventory.releaseCount).isEqualTo(1);
    }

    @Test
    void confirmIdempotenteEnTerminalDevuelveElMismo() {
        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));
        payments.approved = true;
        Checkout confirmed = service.confirm(customerId, checkout.getId());
        int ordersCreated = orders.createCount;

        Checkout retry = service.confirm(customerId, checkout.getId());

        assertThat(retry.getId()).isEqualTo(confirmed.getId());
        assertThat(orders.createCount).isEqualTo(ordersCreated);
    }

    @Test
    void cancelLiberaReservasYCancela() {
        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));

        Checkout cancelled = service.cancel(customerId, checkout.getId());

        assertThat(cancelled.getStatus()).isEqualTo(CheckoutStatus.CANCELLED);
        assertThat(inventory.releaseCount).isEqualTo(1);
    }

    @Test
    void getByIdDeOtroClienteLanzaNotFound() {
        Checkout checkout = service.start(customerId, "key-1", new StartCheckoutCommand(UUID.randomUUID()));

        assertThatThrownBy(() -> service.getById(UUID.randomUUID(), checkout.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeCheckoutRepository implements CheckoutRepository {

        private final Map<String, Checkout> byKey = new HashMap<>();
        private final Map<CheckoutId, Checkout> byId = new HashMap<>();

        @Override
        public Optional<Checkout> findByIdempotencyKey(String idempotencyKey) {
            return Optional.ofNullable(byKey.get(idempotencyKey));
        }

        @Override
        public Optional<Checkout> findById(CheckoutId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Checkout> saveIfAbsent(Checkout checkout) {
            if (byKey.containsKey(checkout.getIdempotencyKey())) {
                return Optional.of(byKey.get(checkout.getIdempotencyKey()));
            }
            save(checkout);
            return Optional.empty();
        }

        @Override
        public Checkout save(Checkout checkout) {
            byKey.put(checkout.getIdempotencyKey(), checkout);
            byId.put(checkout.getId(), checkout);
            return checkout;
        }
    }

    private static final class FakeCartProvider implements CartProvider {
        private CartData cart;

        @Override
        public CartData getCart(UUID customerId) {
            return cart;
        }
    }

    private static final class FakeProductProvider implements ProductProvider {
        @Override
        public ProductInfo productInfoOf(ProductId productId) {
            return new ProductInfo("CAMISA", "Camisa");
        }
    }

    private static final class FakePriceProvider implements PriceProvider {
        @Override
        public Money currentPrice(ProductId productId, org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId variantId) {
            return usd(900);
        }
    }

    private static final class FakeInventoryReservationPort implements InventoryReservationPort {
        private int reserveCount;
        private int releaseCount;

        @Override
        public UUID reserve(ProductId productId, org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId variantId,
                            int quantity, Instant expiresAt) {
            reserveCount++;
            return UUID.randomUUID();
        }

        @Override
        public void release(ProductId productId, org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId variantId,
                            UUID reservationId) {
            releaseCount++;
        }
    }

    private static final class FakeCustomerProvider implements CustomerProvider {
        private boolean fail;

        @Override
        public String addressSnapshotOf(UUID customerId, UUID addressId) {
            if (fail) {
                throw new ResourceNotFoundException("Dirección no encontrada");
            }
            return "Calle 1, Madrid, España";
        }
    }

    private static final class FakeCouponProvider implements CouponProvider {
        private Money discount;

        @Override
        public Money discountFor(UUID customerId, String code, List<CouponItemDto> items) {
            return discount;
        }
    }

    private static final class FakeOrderPort implements OrderPort {
        private int createCount;
        private final List<OrderId> confirmed = new java.util.ArrayList<>();
        private final List<OrderId> cancelled = new java.util.ArrayList<>();

        @Override
        public OrderId createOrder(OrderData data) {
            createCount++;
            return OrderId.generate();
        }

        @Override
        public void confirm(OrderId orderId) {
            confirmed.add(orderId);
        }

        @Override
        public void cancel(OrderId orderId) {
            cancelled.add(orderId);
        }
    }

    private static final class FakePaymentPort implements PaymentPort {
        private boolean approved = true;

        @Override
        public PaymentResult initiate(OrderId orderId, Money amount, String requestId) {
            return new PaymentResult(UUID.randomUUID(), approved);
        }
    }
}