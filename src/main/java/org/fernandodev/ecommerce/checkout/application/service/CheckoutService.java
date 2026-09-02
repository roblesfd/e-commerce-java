package org.fernandodev.ecommerce.checkout.application.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.CartData;
import org.fernandodev.ecommerce.checkout.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.checkout.application.dto.OrderData;
import org.fernandodev.ecommerce.checkout.application.dto.ProductInfo;
import org.fernandodev.ecommerce.checkout.application.dto.StartCheckoutCommand;
import org.fernandodev.ecommerce.checkout.application.port.in.CheckoutUseCases;
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
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutStatus;
import org.fernandodev.ecommerce.checkout.domain.value_object.ReservationReference;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Checkout.
 * <p>
 * Orquesta la conversión del carrito en una orden con pago a través de puertos
 * hacia Cart, Catalog, Pricing, Inventory, Customer, Promotions, Order y la
 * pasarela de pago. La idempotencia se garantiza con una clave única
 * (Idempotency-Key) persistida con SETNX.
 * </p>
 */
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCases {

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(30);

    private final CheckoutRepository checkouts;
    private final CartProvider carts;
    private final ProductProvider products;
    private final PriceProvider prices;
    private final InventoryReservationPort inventory;
    private final CustomerProvider customers;
    private final CouponProvider coupons;
    private final OrderPort orders;
    private final PaymentPort payments;

    @Override
    public Checkout start(UUID customerId, String idempotencyKey, StartCheckoutCommand command) {
        Optional<Checkout> existing = checkouts.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }
        CartData cart = carts.getCart(customerId);
        if (cart.items().isEmpty()) {
            throw new BusinessRuleViolationException("El carrito no puede estar vacío");
        }
        String addressSnapshot = customers.addressSnapshotOf(customerId, command.addressId());

        List<ReservationReference> reservations = new ArrayList<>();
        List<CheckoutItem> checkoutItems = new ArrayList<>();
        Money subtotal = Money.zero(cart.items().get(0).unitPrice().currencyCode());
        try {
            for (CartData.CartItemData item : cart.items()) {
                ProductInfo info = products.productInfoOf(item.productId());
                Money currentPrice = prices.currentPrice(item.productId(), item.variantId());
                UUID reservationId = inventory.reserve(item.productId(), item.variantId(), item.quantity(),
                        Instant.now().plus(RESERVATION_TTL));
                reservations.add(new ReservationReference(item.productId(), item.variantId(), reservationId));
                checkoutItems.add(new CheckoutItem(item.productId(), item.variantId(), item.quantity(), currentPrice));
                subtotal = subtotal.add(currentPrice.multiply(BigDecimal.valueOf(item.quantity())));
            }
        } catch (RuntimeException e) {
            releaseAll(reservations);
            throw e;
        }

        String couponCode = null;
        Money couponDiscount = null;
        if (cart.coupon() != null) {
            couponCode = cart.coupon().code();
            List<CouponItemDto> couponItems = checkoutItems.stream()
                    .map(item -> new CouponItemDto(item.productId(), item.quantity(), item.unitPrice()))
                    .toList();
            couponDiscount = coupons.discountFor(customerId, couponCode, couponItems);
        }

        Checkout checkout = Checkout.start(
                CheckoutId.generate(),
                customerId,
                idempotencyKey,
                cart.cartId(),
                checkoutItems,
                reservations,
                couponCode,
                couponDiscount,
                subtotal,
                command.addressId(),
                addressSnapshot,
                Instant.now());

        Optional<Checkout> concurrent = checkouts.saveIfAbsent(checkout);
        if (concurrent.isPresent()) {
            releaseAll(reservations);
            return concurrent.get();
        }
        return checkout;
    }

    @Override
    public Checkout getById(UUID customerId, CheckoutId id) {
        return getOwned(customerId, id);
    }

    @Override
    public Checkout confirm(UUID customerId, CheckoutId id) {
        Checkout checkout = getOwned(customerId, id);
        if (checkout.isTerminal()) {
            return checkout;
        }
        if (checkout.getStatus() != CheckoutStatus.PENDING) {
            throw new BusinessRuleViolationException("El checkout está en curso; reintente o consulte su estado");
        }

        List<OrderData.OrderItemData> orderItems = checkout.getItems().stream()
                .map(item -> {
                    ProductInfo info = products.productInfoOf(item.productId());
                    return new OrderData.OrderItemData(
                            item.productId(), item.variantId(), info.sku(), info.name(),
                            item.quantity(), item.unitPrice());
                })
                .toList();
        OrderId orderId = orders.createOrder(new OrderData(
                checkout.getCustomerId(),
                checkout.getAddressId(),
                checkout.getAddressSnapshot(),
                checkout.getCouponCode(),
                orderItems,
                checkout.getSubtotal(),
                checkout.getDiscounts(),
                checkout.getTotal()));
        checkout.markOrderCreated(orderId, Instant.now());

        PaymentPort.PaymentResult payment = payments.initiate(
                orderId, checkout.getTotal(), checkout.getIdempotencyKey());
        checkout.markPaymentInitiated(payment.paymentId(), Instant.now());

        if (payment.approved()) {
            orders.confirm(orderId);
            checkout.confirm(Instant.now());
        } else {
            orders.cancel(orderId);
            releaseAll(checkout.getReservations());
            checkout.fail(Instant.now());
        }
        return checkouts.save(checkout);
    }

    @Override
    public Checkout cancel(UUID customerId, CheckoutId id) {
        Checkout checkout = getOwned(customerId, id);
        if (checkout.getStatus() != CheckoutStatus.PENDING) {
            throw new BusinessRuleViolationException("Solo se puede cancelar un checkout PENDING");
        }
        releaseAll(checkout.getReservations());
        checkout.cancel(Instant.now());
        return checkouts.save(checkout);
    }

    private Checkout getOwned(UUID customerId, CheckoutId id) {
        Checkout checkout = checkouts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout no encontrado: " + id));
        if (!checkout.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Checkout no encontrado: " + id);
        }
        return checkout;
    }

    private void releaseAll(List<ReservationReference> reservations) {
        for (ReservationReference reservation : reservations) {
            try {
                inventory.release(reservation.productId(), reservation.variantId(), reservation.reservationId());
            } catch (RuntimeException ignored) {
                // liberación best-effort
            }
        }
    }
}