package org.fernandodev.ecommerce.checkout.adapter.out.persistence.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.application.port.out.CheckoutRepository;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CheckoutRedisRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private CheckoutRepository repository;

    @Test
    void guardaYRecuperaPorIdempotencyKey() {
        Checkout checkout = checkout(uniqueKey());

        repository.save(checkout);
        Checkout loaded = repository.findByIdempotencyKey(checkout.getIdempotencyKey()).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(checkout.getId());
        assertThat(loaded.getStatus()).isEqualTo(CheckoutStatus.PENDING);
        assertThat(loaded.getItems()).hasSize(1);
        assertThat(loaded.getSubtotal()).isEqualTo(usd(1800));
    }

    @Test
    void findByIdDevuelveElCheckout() {
        Checkout checkout = checkout(uniqueKey());
        repository.save(checkout);

        assertThat(repository.findById(checkout.getId())).isPresent();
    }

    @Test
    void findByIdempotencyKeyInexistenteDevuelveVacio() {
        assertThat(repository.findByIdempotencyKey("no-existe-" + UUID.randomUUID())).isEmpty();
    }

    @Test
    void saveIfAbsentDevuelveElExistenteSiLaClaveEstaOcupada() {
        String key = uniqueKey();
        Checkout first = checkout(key);
        Checkout second = checkout(key);

        Optional<Checkout> existing = repository.saveIfAbsent(first);
        assertThat(existing).isEmpty();

        Optional<Checkout> onRetry = repository.saveIfAbsent(second);
        assertThat(onRetry).isPresent();
        assertThat(onRetry.get().getId()).isEqualTo(first.getId());
    }

    private static String uniqueKey() {
        return "key-" + UUID.randomUUID();
    }

    private static Checkout checkout(String key) {
        return Checkout.start(CheckoutId.generate(), UUID.randomUUID(), key, UUID.randomUUID(),
                List.of(new CheckoutItem(ProductId.generate(), null, 2, usd(900))),
                List.of(), null, null, usd(1800), null, null, T0);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}