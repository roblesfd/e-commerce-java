package org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.dto.ReserveStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verifica que bajo reservas concurrentes con stock = 1 exactamente una reserva
 * tiene éxito (prevención de overselling con bloqueo pesimista).
 */
class InventoryReservationConcurrencyTest extends AbstractIntegrationTest {

    private static final Instant EXPIRES = Instant.parse("2099-12-31T00:00:00Z");

    @Autowired
    private InventoryUseCases useCases;

    @Test
    void soloUnaReservaConcurrenteTieneExitoConStockUno() throws Exception {
        ProductId productId = ProductId.generate();
        useCases.increaseStock(productId, null, new IncreaseStockCommand(1, "reposición"));

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger insufficientStock = new AtomicInteger();

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
                try {
                    useCases.reserveStock(productId, null, new ReserveStockCommand(1, EXPIRES));
                    success.incrementAndGet();
                } catch (BusinessRuleViolationException e) {
                    insufficientStock.incrementAndGet();
                }
            }));
        }
        ready.await();
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(success.get()).isEqualTo(1);
        assertThat(insufficientStock.get()).isEqualTo(threads - 1);
        assertThat(useCases.availableStock(productId, null)).isZero();
    }
}