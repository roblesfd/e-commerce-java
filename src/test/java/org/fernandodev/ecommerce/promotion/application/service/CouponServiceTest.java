package org.fernandodev.ecommerce.promotion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.CartItemDto;
import org.fernandodev.ecommerce.promotion.application.dto.CouponValidationResult;
import org.fernandodev.ecommerce.promotion.application.dto.CreateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.dto.UpdateCouponCommand;
import org.fernandodev.ecommerce.promotion.application.port.out.CouponRepository;
import org.fernandodev.ecommerce.promotion.application.port.out.ProductCategoryProvider;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponApplication;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class CouponServiceTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2099-12-31T00:00:00Z");

    private final FakePromotionRepository promotions = new FakePromotionRepository();
    private final FakeCouponRepository coupons = new FakeCouponRepository();
    private final CouponService service = new CouponService(coupons, promotions, new FakeProductCategoryProvider());

    @Test
    void createGuardaCupon() {
        PromotionId promotionId = guardarPromocion();

        Coupon coupon = service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2));

        assertThat(coupon.getCode()).isEqualTo("WELCOME10");
        assertThat(coupon.getPromotionId()).isEqualTo(promotionId);
        assertThat(coupons.findById(coupon.getId())).contains(coupon);
    }

    @Test
    void createConCodigoDuplicadoLanzaConflicto() {
        PromotionId promotionId = guardarPromocion();
        service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2));

        assertThatThrownBy(() -> service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createConPromocionInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.create(new CreateCouponCommand("WELCOME10", PromotionId.generate(), T1, T2)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(CouponId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateYDeactivateCambianElEstado() {
        PromotionId promotionId = guardarPromocion();
        Coupon coupon = service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2));

        Coupon desactivado = service.deactivate(coupon.getId());
        assertThat(desactivado.isActiveAt(Instant.now())).isFalse();

        Coupon activado = service.activate(coupon.getId());
        assertThat(activado.isActiveAt(Instant.now())).isTrue();
    }

    @Test
    void updateCambiaLaValidez() {
        PromotionId promotionId = guardarPromocion();
        Coupon coupon = service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2));

        Coupon updated = service.update(coupon.getId(), new UpdateCouponCommand(T1, Instant.parse("2027-12-31T00:00:00Z")));

        assertThat(updated.getValidTo()).isEqualTo(Instant.parse("2027-12-31T00:00:00Z"));
    }

    @Test
    void validateCouponValidoDevuelveDescuento() {
        PromotionId promotionId = guardarPromocion();
        service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2));

        CouponValidationResult result = service.validateCoupon("WELCOME10", UUID.randomUUID(),
                new ApplyCouponCommand("WELCOME10", items(1000)));

        assertThat(result.valid()).isTrue();
        assertThat(result.getDiscount()).contains(usd(100));
    }

    @Test
    void validateCouponCodigoInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.validateCoupon("NOEXISTE", UUID.randomUUID(),
                new ApplyCouponCommand("NOEXISTE", items(1000))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validateCouponConMinimoNoAlcanzadoDevuelveInvalido() {
        PromotionId promotionId = promotions.save(Promotion.create(PromotionId.generate(), "Min", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, usd(500), null, 0, 0, Set.of(), Set.of())).getId();
        service.create(new CreateCouponCommand("MIN10", promotionId, T1, T2));

        CouponValidationResult result = service.validateCoupon("MIN10", UUID.randomUUID(),
                new ApplyCouponCommand("MIN10", items(200)));

        assertThat(result.valid()).isFalse();
        assertThat(result.getReason().orElse("")).contains("monto mínimo");
    }

    @Test
    void applyCouponDevuelveLaAplicacion() {
        PromotionId promotionId = guardarPromocion();
        service.create(new CreateCouponCommand("WELCOME10", promotionId, T1, T2));

        CouponApplication application = service.applyCoupon("WELCOME10", UUID.randomUUID(),
                new ApplyCouponCommand("WELCOME10", items(1000)));

        assertThat(application.promotionId()).isEqualTo(promotionId);
        assertThat(application.discount()).isEqualTo(usd(100));
    }

    @Test
    void applyCouponConCarritoVacioLanzaViolacion() {
        assertThatThrownBy(() -> service.applyCoupon("X", UUID.randomUUID(),
                new ApplyCouponCommand("X", List.of())))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void applyCouponNoElegibleLanzaViolacion() {
        PromotionId promotionId = promotions.save(Promotion.create(PromotionId.generate(), "Scoped", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 0,
                Set.of(ProductId.generate()), Set.of())).getId();
        service.create(new CreateCouponCommand("SCOPED10", promotionId, T1, T2));

        assertThatThrownBy(() -> service.applyCoupon("SCOPED10", UUID.randomUUID(),
                new ApplyCouponCommand("SCOPED10", items(1000))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private PromotionId guardarPromocion() {
        return promotions.save(Promotion.create(PromotionId.generate(), "10%", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 0, Set.of(), Set.of())).getId();
    }

    private static List<CartItemDto> items(int unitPrice) {
        return List.of(new CartItemDto(ProductId.generate(), 1, usd(unitPrice)));
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakePromotionRepository implements PromotionRepository {

        private final Map<PromotionId, Promotion> byId = new HashMap<>();

        @Override
        public Promotion save(Promotion promotion) {
            byId.put(promotion.getId(), promotion);
            return promotion;
        }

        @Override
        public Optional<Promotion> findById(PromotionId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Promotion> findByIdForUpdate(PromotionId id) {
            return findById(id);
        }

        @Override
        public List<Promotion> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public boolean existsById(PromotionId id) {
            return byId.containsKey(id);
        }
    }

    private static final class FakeCouponRepository implements CouponRepository {

        private final Map<CouponId, Coupon> byId = new HashMap<>();

        @Override
        public Coupon save(Coupon coupon) {
            byId.put(coupon.getId(), coupon);
            return coupon;
        }

        @Override
        public Optional<Coupon> findById(CouponId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Coupon> findByCode(String code) {
            return byId.values().stream().filter(c -> c.getCode().equals(code)).findFirst();
        }

        @Override
        public boolean existsByCode(String code) {
            return findByCode(code).isPresent();
        }

        @Override
        public List<Coupon> findAll() {
            return List.copyOf(byId.values());
        }
    }

    private static final class FakeProductCategoryProvider implements ProductCategoryProvider {

        @Override
        public Set<CategoryId> categoriesOf(ProductId productId) {
            return Set.of();
        }
    }
}