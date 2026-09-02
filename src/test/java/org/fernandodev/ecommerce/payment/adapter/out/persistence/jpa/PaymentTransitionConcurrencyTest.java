package org.fernandodev.ecommerce.payment.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verifica que las capturas concurrentes no se duplican (máquina de estados +
 * bloqueo pesimista): exactamente una transición CAPTURED se aplica.
 */
class PaymentTransitionConcurrencyTest extends AbstractIntegrationTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private PaymentRepository repository;

    @Autowired
    private PaymentUseCases useCases;

    @Test
    void capturaConcurrenteNoSeDuplica() throws Exception {
        Payment payment = Payment.create(PaymentId.generate(), OrderId.generate(),
                "req-" + UUID.randomUUID(), Money.of("1000", "USD"), T0);
        payment.authorize("txn-1", true, T0);
        repository.save(payment);

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                useCases.capture(payment.getId());
            }));
        }
        ready.await();
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        Payment finalPayment = useCases.getById(payment.getId());
        assertThat(finalPayment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        long capturedEvents = finalPayment.getEvents().stream()
                .filter(event -> event.type().equals("CAPTURED"))
                .count();
        assertThat(capturedEvents).isEqualTo(1);
    }
}