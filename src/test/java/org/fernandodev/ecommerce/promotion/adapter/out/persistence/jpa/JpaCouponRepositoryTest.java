package org.fernandodev.ecommerce.promotion.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.promotion.application.port.out.CouponRepository;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JpaCouponRepositoryTest extends AbstractIntegrationTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2099-12-31T00:00:00Z");

    @Autowired
    private CouponRepository repository;

    @Autowired
    private PromotionRepository promotions;

    @Test
    void guardaYRecuperaPorCodigo() {
        PromotionId promotionId = promotions.save(Promotion.create(PromotionId.generate(), "10%",
                DiscountType.PERCENTAGE, T1, T2, new BigDecimal("10"), null, List.of(), 0, 0,
                null, null, 0, 0, Set.of(), Set.of())).getId();
        Coupon coupon = Coupon.create(CouponId.generate(), "WELCOME10", promotionId, T1, T2);

        repository.save(coupon);
        Coupon loaded = repository.findByCode("WELCOME10").orElseThrow();

        assertThat(loaded.getId()).isEqualTo(coupon.getId());
        assertThat(loaded.getPromotionId()).isEqualTo(promotionId);
    }

    @Test
    void existsByCodeTrasGuardar() {
        PromotionId promotionId = promotions.save(Promotion.create(PromotionId.generate(), "10%",
                DiscountType.PERCENTAGE, T1, T2, new BigDecimal("10"), null, List.of(), 0, 0,
                null, null, 0, 0, Set.of(), Set.of())).getId();
        repository.save(Coupon.create(CouponId.generate(), "WELCOME10", promotionId, T1, T2));

        assertThat(repository.existsByCode("WELCOME10")).isTrue();
        assertThat(repository.existsByCode("OTRO")).isFalse();
    }
}