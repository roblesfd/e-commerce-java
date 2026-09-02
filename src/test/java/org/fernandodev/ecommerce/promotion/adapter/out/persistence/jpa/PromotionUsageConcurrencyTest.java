package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.promotion.application.dto.CreatePromotionCommand;
import org.fernandodev.ecommerce.promotion.application.port.in.PromotionUseCases;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verifica que el límite de uso de una promoción se respeta bajo concurrencia
 * (bloqueo pesimista en recordUsage).
 */
class PromotionUsageConcurrencyTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2099-12-31T00:00:00Z");

    @Autowired
    private PromotionUseCases useCases;

    @Test
    void soloSePermiteElMaximoDeUsosConcurrentes() throws Exception {
        int maxUsage = 3;
        var promotion = useCases.create(new CreatePromotionCommand(
                "Promo", DiscountType.PERCENTAGE, new BigDecimal("10"), null, List.of(), 0, 0,
                T1, T2, null, null, maxUsage, 0, Set.of(), Set.of()));

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger limitReached = new AtomicInteger();

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
                    useCases.recordUsage(promotion.getId(), UUID.randomUUID());
                    success.incrementAndGet();
                } catch (BusinessRuleViolationException e) {
                    limitReached.incrementAndGet();
                }
            }));
        }
        ready.await();
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(success.get()).isEqualTo(maxUsage);
        assertThat(limitReached.get()).isEqualTo(threads - maxUsage);
        assertThat(useCases.getById(promotion.getId()).getUsedCount()).isEqualTo(maxUsage);
    }
}