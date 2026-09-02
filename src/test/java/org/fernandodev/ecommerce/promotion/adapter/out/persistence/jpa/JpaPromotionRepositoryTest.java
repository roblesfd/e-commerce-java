package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.promotion.domain.value_object.VolumeTier;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaPromotionRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2099-12-31T00:00:00Z");

    @Autowired
    private PromotionRepository repository;

    @Test
    void guardaYRecuperaPromocionConScopeTramosYUso() {
        PromotionId id = PromotionId.generate();
        ProductId productId = ProductId.generate();
        Promotion promotion = Promotion.create(id, "Volumen", DiscountType.VOLUME,
                T1, T2, null, null,
                List.of(new VolumeTier(3, DiscountType.PERCENTAGE, new BigDecimal("10"))),
                0, 0, Money.of("500", "USD"), Money.of("200", "USD"), 10, 2,
                Set.of(productId), Set.of());
        promotion.recordUsage(UUID.randomUUID());

        repository.save(promotion);
        Promotion loaded = repository.findById(id).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Volumen");
        assertThat(loaded.getType()).isEqualTo(DiscountType.VOLUME);
        assertThat(loaded.getVolumeTiers()).hasSize(1);
        assertThat(loaded.getProductIds()).contains(productId);
        assertThat(loaded.getMinPurchaseAmount()).isEqualTo(Money.of("500", "USD"));
        assertThat(loaded.getMaxDiscount()).isEqualTo(Money.of("200", "USD"));
        assertThat(loaded.getUsedCount()).isEqualTo(1);
        assertThat(loaded.getUsageByCustomer()).hasSize(1);
    }

    @Test
    void findByIdForUpdateDevuelveLaPromocion() {
        Promotion promotion = repository.save(Promotion.create(PromotionId.generate(), "10%",
                DiscountType.PERCENTAGE, T1, T2, new BigDecimal("10"), null, List.of(), 0, 0,
                null, null, 0, 0, Set.of(), Set.of()));

        assertThat(repository.findByIdForUpdate(promotion.getId()).orElseThrow().getId())
                .isEqualTo(promotion.getId());
    }

    @Test
    void savePersisteElRegistroDeUso() {
        Promotion promotion = repository.save(Promotion.create(PromotionId.generate(), "10%",
                DiscountType.PERCENTAGE, T1, T2, new BigDecimal("10"), null, List.of(), 0, 0,
                null, null, 10, 10, Set.of(), Set.of()));
        UUID customerId = UUID.randomUUID();
        promotion.recordUsage(customerId);
        repository.save(promotion);

        Promotion loaded = repository.findById(promotion.getId()).orElseThrow();
        assertThat(loaded.getUsedCount()).isEqualTo(1);
        assertThat(loaded.getUsageByCustomer().get(0).count()).isEqualTo(1);
    }
}