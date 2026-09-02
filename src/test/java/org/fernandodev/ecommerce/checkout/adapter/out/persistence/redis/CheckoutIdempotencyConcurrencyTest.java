package org.fernandodev.ecommerce.checkout.adapter.out.persistence.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.application.port.out.CheckoutRepository;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verifica que la idempotencia (SETNX sobre la clave de idempotencia) garantiza
 * exactamente un checkout creado bajo peticiones concurrentes con la misma key.
 */
class CheckoutIdempotencyConcurrencyTest extends AbstractIntegrationTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private CheckoutRepository repository;

    @Test
    void soloUnCheckoutSeCreaConLaMismaKeyConcurrente() throws Exception {
        String key = "key-concurrente";
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger created = new AtomicInteger();
        List<UUID> returnedIds = new ArrayList<>();

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
                Checkout checkout = checkout(key);
                var existing = repository.saveIfAbsent(checkout);
                if (existing.isEmpty()) {
                    created.incrementAndGet();
                }
                synchronized (returnedIds) {
                    returnedIds.add(checkout.getId().value());
                }
            }));
        }
        ready.await();
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(created.get()).isEqualTo(1);
        assertThat(returnedIds).doesNotHaveDuplicates();
        assertThat(repository.findByIdempotencyKey(key)).isPresent();
    }

    private static Checkout checkout(String key) {
        return Checkout.start(CheckoutId.generate(), UUID.randomUUID(), key, UUID.randomUUID(),
                List.of(new CheckoutItem(ProductId.generate(), null, 1, usd(100))),
                List.of(), null, null, usd(100), null, null, T0);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}