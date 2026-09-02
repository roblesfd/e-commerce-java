package org.fernandodev.ecommerce.promotion.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.domain.value_object.CartContext;
import org.fernandodev.ecommerce.promotion.domain.value_object.CartItemLine;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.promotion.domain.value_object.VolumeTier;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class PromotionTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant T3 = Instant.parse("2026-03-01T00:00:00Z");

    @Test
    void createPercentageEsActivaYConElegibilidad() {
        Promotion promotion = porcentaje("10");

        assertThat(promotion.isActive()).isTrue();
        assertThat(promotion.isActiveAt(T1)).isTrue();
        assertThat(promotion.ineligibilityReason(carrito(unPrecio(1000)), UUID.randomUUID(), T1)).isEmpty();
    }

    @Test
    void createConPeriodoInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> Promotion.create(PromotionId.generate(), "10%", DiscountType.PERCENTAGE,
                T2, T1, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 0, Set.of(), Set.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createPercentageSinValorLanzaExcepcion() {
        assertThatThrownBy(() -> Promotion.create(PromotionId.generate(), "10%", DiscountType.PERCENTAGE,
                T1, T2, null, null, List.of(), 0, 0, null, null, 0, 0, Set.of(), Set.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void isActiveAtRespetaPeriodoYEstado() {
        Promotion promotion = porcentaje("10");

        assertThat(promotion.isActiveAt(T1)).isTrue();
        assertThat(promotion.isActiveAt(T2)).isFalse();

        promotion.deactivate();
        assertThat(promotion.isActiveAt(T1)).isFalse();
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Promotion promotion = porcentaje("10");

        promotion.deactivate();
        assertThat(promotion.isActive()).isFalse();

        promotion.activate();
        assertThat(promotion.isActive()).isTrue();
    }

    @Test
    void ineligibilityReasonConMinimoNoAlcanzado() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0,
                usd(500), null, 0, 0, Set.of(), Set.of());

        assertThat(promotion.ineligibilityReason(carrito(unPrecio(200)), UUID.randomUUID(), T1).orElse(""))
                .contains("monto mínimo");
    }

    @Test
    void ineligibilityReasonConLimiteDeUsoAlcanzado() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 1, 0, Set.of(), Set.of());
        promotion.recordUsage(UUID.randomUUID());

        assertThat(promotion.ineligibilityReason(carrito(unPrecio(1000)), UUID.randomUUID(), T1).orElse(""))
                .contains("límite de uso");
    }

    @Test
    void ineligibilityReasonConLimitePorClienteAlcanzado() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 1, Set.of(), Set.of());
        UUID customerId = UUID.randomUUID();
        promotion.recordUsage(customerId);

        assertThat(promotion.ineligibilityReason(carrito(unPrecio(1000)), customerId, T1).orElse(""))
                .contains("por cliente");
    }

    @Test
    void ineligibilityReasonConScopeProductoQueNoCoincide() {
        ProductId otroProducto = ProductId.generate();
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 0,
                Set.of(ProductId.generate()), Set.of());

        assertThat(promotion.ineligibilityReason(carrito(unPrecio(1000)), UUID.randomUUID(), T1).orElse(""))
                .contains("alcance");
    }

    @Test
    void computeDiscountPercentage() {
        Promotion promotion = porcentaje("10");

        assertThat(promotion.computeDiscount(carrito(unPrecio(1000)), T1)).isEqualTo(usd(100));
    }

    @Test
    void computeDiscountFixedAmountSeAcotaAlSubtotal() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Fijo", DiscountType.FIXED_AMOUNT,
                T1, T2, null, usd(200), List.of(), 0, 0, null, null, 0, 0, Set.of(), Set.of());

        assertThat(promotion.computeDiscount(carrito(unPrecio(1000)), T1)).isEqualTo(usd(200));
        assertThat(promotion.computeDiscount(carrito(unPrecio(100)), T1)).isEqualTo(usd(100));
    }

    @Test
    void computeDiscountRespectaMaxDiscount() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("20"), null, List.of(), 0, 0, null, usd(150), 0, 0, Set.of(), Set.of());

        assertThat(promotion.computeDiscount(carrito(unPrecio(1000)), T1)).isEqualTo(usd(150));
    }

    @Test
    void volumeDiscountAplicaElTramoMayor() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Volumen", DiscountType.VOLUME,
                T1, T2, null, null,
                List.of(new VolumeTier(3, DiscountType.PERCENTAGE, new BigDecimal("10")),
                        new VolumeTier(5, DiscountType.PERCENTAGE, new BigDecimal("20"))),
                0, 0, null, null, 0, 0, Set.of(), Set.of());

        assertThat(promotion.computeDiscount(carrito(4), T1)).isEqualTo(usd(40));
        assertThat(promotion.computeDiscount(carrito(6), T1)).isEqualTo(usd(120));
    }

    @Test
    void buyXGetYDescuentaLineasElegibles() {
        ProductId productId = ProductId.generate();
        Promotion promotion = Promotion.create(PromotionId.generate(), "2x1", DiscountType.BUY_X_GET_Y,
                T1, T2, null, null, List.of(), 2, 1, null, null, 0, 0,
                Set.of(productId), Set.of());

        CartContext cart = new CartContext(List.of(
                new CartItemLine(productId, Set.of(), 6, usd(100))));

        assertThat(promotion.computeDiscount(cart, T1)).isEqualTo(usd(200));
    }

    @Test
    void recordUsageIncrementaTotalYPorCliente() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 10, 10, Set.of(), Set.of());
        UUID customerId = UUID.randomUUID();

        promotion.recordUsage(customerId);
        promotion.recordUsage(customerId);

        assertThat(promotion.getUsedCount()).isEqualTo(2);
        assertThat(promotion.getUsageByCustomer().get(0).count()).isEqualTo(2);
    }

    @Test
    void recordUsageConLimiteAlcanzadoLanzaExcepcion() {
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 1, 0, Set.of(), Set.of());
        promotion.recordUsage(UUID.randomUUID());

        assertThatThrownBy(() -> promotion.recordUsage(UUID.randomUUID()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void scopePorCategoriaEsElegibleSiCoincide() {
        CategoryId categoria = CategoryId.generate();
        ProductId productId = ProductId.generate();
        Promotion promotion = Promotion.create(PromotionId.generate(), "Promo", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 0,
                Set.of(), Set.of(categoria));

        CartContext cart = new CartContext(List.of(
                new CartItemLine(productId, Set.of(categoria), 1, usd(1000))));

        assertThat(promotion.ineligibilityReason(cart, UUID.randomUUID(), T1)).isEmpty();
        assertThat(promotion.computeDiscount(cart, T1)).isEqualTo(usd(100));
    }

    private static Promotion porcentaje(String value) {
        return Promotion.create(PromotionId.generate(), "Promo " + value, DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal(value), null, List.of(), 0, 0, null, null, 0, 0, Set.of(), Set.of());
    }

    private static CartContext carrito(Money subtotal) {
        return new CartContext(List.of(new CartItemLine(ProductId.generate(), Set.of(), 1, subtotal)));
    }

    private static CartContext carrito(int cantidad) {
        return new CartContext(List.of(new CartItemLine(ProductId.generate(), Set.of(), cantidad, usd(100))));
    }

    private static Money unPrecio(int amount) {
        return usd(amount);
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}