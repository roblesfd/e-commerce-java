package org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaPaymentRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private PaymentRepository repository;

    @Test
    void guardaYRecuperaPagoConEventos() {
        OrderId orderId = OrderId.generate();
        Payment payment = Payment.create(PaymentId.generate(), orderId, "req-1", usd(1000), T1);
        payment.authorize("txn-1", true, T1);
        payment.capture("txn-2", T1);

        repository.save(payment);
        Payment loaded = repository.findById(payment.getId()).orElseThrow();

        assertThat(loaded.getOrderId()).isEqualTo(orderId);
        assertThat(loaded.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(loaded.getProviderTransactionId()).isEqualTo("txn-2");
        assertThat(loaded.getEvents()).hasSize(3);
        assertThat(loaded.getEvents()).extracting(e -> e.type())
                .contains("CREATED", "AUTHORIZED", "CAPTURED");
    }

    @Test
    void findByOrderIdDevuelveElPago() {
        OrderId orderId = OrderId.generate();
        Payment payment = repository.save(Payment.create(PaymentId.generate(), orderId, "req-1", usd(1000), T1));

        assertThat(repository.findByOrderId(orderId).orElseThrow().getId()).isEqualTo(payment.getId());
    }

    @Test
    void findByProviderTransactionIdDevuelveElPago() {
        Payment payment = repository.save(Payment.create(PaymentId.generate(), OrderId.generate(), "req-1", usd(1000), T1));
        payment.authorize("txn-abc", true, T1);
        repository.save(payment);

        assertThat(repository.findByProviderTransactionId("txn-abc").orElseThrow().getId())
                .isEqualTo(payment.getId());
    }

    @Test
    void existsByRequestIdTrasGuardar() {
        repository.save(Payment.create(PaymentId.generate(), OrderId.generate(), "req-1", usd(1000), T1));

        assertThat(repository.existsByRequestId("req-1")).isTrue();
        assertThat(repository.existsByRequestId("req-2")).isFalse();
    }

    @Test
    void findByIdForUpdateDevuelveElPago() {
        Payment payment = repository.save(Payment.create(PaymentId.generate(), OrderId.generate(), "req-1", usd(1000), T1));

        assertThat(repository.findByIdForUpdate(payment.getId()).orElseThrow().getId())
                .isEqualTo(payment.getId());
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}