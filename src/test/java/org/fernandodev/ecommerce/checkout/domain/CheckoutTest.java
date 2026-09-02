package org.fernandodev.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutStatus;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class CheckoutTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    void startCreaCheckoutPendingConTotales() {
        Checkout checkout = checkoutConCupon();

        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.PENDING);
        assertThat(checkout.getItems()).hasSize(1);
        assertThat(checkout.getSubtotal()).isEqualTo(usd(1800));
        assertThat(checkout.getDiscounts()).isEqualTo(usd(180));
        assertThat(checkout.getTotal()).isEqualTo(usd(1620));
        assertThat(checkout.getCouponCode()).isEqualTo("WELCOME10");
    }

    @Test
    void startSinCuponDescuentaCero() {
        Checkout checkout = Checkout.start(CheckoutId.generate(), UUID.randomUUID(), "key-1",
                UUID.randomUUID(), List.of(item()), List.of(), null, null, usd(1800), null, null, T1);

        assertThat(checkout.getDiscounts()).isEqualTo(usd(0));
        assertThat(checkout.getTotal()).isEqualTo(usd(1800));
    }

    @Test
    void startSinItemsLanzaExcepcion() {
        assertThatThrownBy(() -> Checkout.start(CheckoutId.generate(), UUID.randomUUID(), "key-1",
                UUID.randomUUID(), List.of(), List.of(), null, null, usd(0), null, null, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void flujoCompletoDeConfirmacion() {
        Checkout checkout = checkoutConCupon();
        OrderId orderId = OrderId.generate();
        UUID paymentId = UUID.randomUUID();

        checkout.markOrderCreated(orderId, T1);
        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.ORDER_CREATED);
        assertThat(checkout.getOrderId()).isEqualTo(orderId);

        checkout.markPaymentInitiated(paymentId, T2);
        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.PAYMENT_PENDING);
        assertThat(checkout.getPaymentId()).isEqualTo(paymentId);

        checkout.confirm(T2);
        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.CONFIRMED);
    }

    @Test
    void falloDePagoMarcaFail() {
        Checkout checkout = checkoutConCupon();
        checkout.markOrderCreated(OrderId.generate(), T1);
        checkout.markPaymentInitiated(UUID.randomUUID(), T2);

        checkout.fail(T2);

        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.FAILED);
        assertThat(checkout.isTerminal()).isTrue();
    }

    @Test
    void cancelarDesdePendingEsValido() {
        Checkout checkout = checkoutConCupon();

        checkout.cancel(T1);

        assertThat(checkout.getStatus()).isEqualTo(CheckoutStatus.CANCELLED);
    }

    @Test
    void transicionesInvalidasLanzanExcepcion() {
        Checkout checkout = checkoutConCupon();

        assertThatThrownBy(() -> checkout.confirm(T1))
                .isInstanceOf(BusinessRuleViolationException.class);

        checkout.cancel(T1);
        assertThatThrownBy(() -> checkout.markPaymentInitiated(UUID.randomUUID(), T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cancelarCheckoutConfirmadoLanzaExcepcion() {
        Checkout checkout = checkoutConCupon();
        checkout.markOrderCreated(OrderId.generate(), T1);
        checkout.markPaymentInitiated(UUID.randomUUID(), T1);
        checkout.confirm(T1);

        assertThatThrownBy(() -> checkout.cancel(T2))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Checkout checkoutConCupon() {
        return Checkout.start(CheckoutId.generate(), UUID.randomUUID(), "key-1", UUID.randomUUID(),
                List.of(item()), List.of(), "WELCOME10", usd(180), usd(1800), null, null, T1);
    }

    private static CheckoutItem item() {
        return new CheckoutItem(ProductId.generate(), null, 2, usd(900));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}