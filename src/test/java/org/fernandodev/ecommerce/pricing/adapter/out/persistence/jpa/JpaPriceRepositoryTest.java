package org.fernandodev.ecommerce.pricing.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceType;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaPriceRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant T3 = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired
    private PriceRepository repository;

    @Test
    void guardaYRecuperaPrecioConHistorialYOverrides() {
        ProductId productId = ProductId.generate();
        Price price = Price.define(PriceId.generate(), productId, null, usd(900));
        price.updateBasePrice(usd(1000), T1);
        price.addOverride(usd(700), T2, T3, PriceType.PROMOTIONAL);

        repository.save(price);
        Price loaded = repository.findById(price.getId()).orElseThrow();

        assertThat(loaded.getProductId()).isEqualTo(productId);
        assertThat(loaded.getBasePrice()).isEqualTo(usd(1000));
        assertThat(loaded.currentPrice(T1)).isEqualTo(usd(1000));
        assertThat(loaded.currentPrice(T2)).isEqualTo(usd(700));
        assertThat(loaded.getHistory()).hasSize(1);
        assertThat(loaded.getHistory().get(0).amount()).isEqualTo(usd(900));
        assertThat(loaded.getOverrides()).hasSize(1);
        assertThat(loaded.getOverrides().get(0).type()).isEqualTo(PriceType.PROMOTIONAL);
        assertThat(loaded.getOverrides().get(0).validFrom()).isEqualTo(T2);
        assertThat(loaded.getOverrides().get(0).validTo()).isEqualTo(T3);
    }

    @Test
    void existsByProductAndVariantTrasGuardar() {
        ProductId productId = ProductId.generate();
        Price price = Price.define(PriceId.generate(), productId, null, usd(900));

        repository.save(price);

        assertThat(repository.existsByProductAndVariant(productId, null)).isTrue();
        assertThat(repository.existsByProductAndVariant(ProductId.generate(), null)).isFalse();
    }

    @Test
    void findByProductAndVariantDistingueVarianteYProducto() {
        ProductId productId = ProductId.generate();
        ProductVariantId variantId = ProductVariantId.generate();
        repository.save(Price.define(PriceId.generate(), productId, null, usd(900)));
        repository.save(Price.define(PriceId.generate(), productId, variantId, usd(950)));

        assertThat(repository.findByProductAndVariant(productId, null).orElseThrow().getBasePrice())
                .isEqualTo(usd(900));
        assertThat(repository.findByProductAndVariant(productId, variantId).orElseThrow().getBasePrice())
                .isEqualTo(usd(950));
    }

    @Test
    void saveActualizaElPrecioExistente() {
        ProductId productId = ProductId.generate();
        Price price = repository.save(Price.define(PriceId.generate(), productId, null, usd(900)));
        price.updateBasePrice(usd(1200), T1);

        Price updated = repository.save(price);

        assertThat(updated.getBasePrice()).isEqualTo(usd(1200));
        assertThat(repository.findById(price.getId()).orElseThrow().getHistory()).hasSize(1);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}