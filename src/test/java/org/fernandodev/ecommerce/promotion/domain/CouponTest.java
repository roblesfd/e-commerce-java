package org.fernandodev.ecommerce.promotion.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class CouponTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-02-01T00:00:00Z");

    @Test
    void createCreaCuponActivo() {
        Coupon coupon = Cupon.cupon("WELCOME10");

        assertThat(coupon.getCode()).isEqualTo("WELCOME10");
        assertThat(coupon.isActive()).isTrue();
        assertThat(coupon.isActiveAt(T1)).isTrue();
        assertThat(coupon.isActiveAt(T2)).isFalse();
    }

    @Test
    void codeSeNormalizaAMayusculas() {
        Coupon coupon = Cupon.cupon(" welcome10 ");

        assertThat(coupon.getCode()).isEqualTo("WELCOME10");
    }

    @Test
    void createConCodigoVacioLanzaExcepcion() {
        assertThatThrownBy(() -> Coupon.create(CouponId.generate(), "  ", PromotionId.generate(), T1, T2))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createConPeriodoInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> Coupon.create(CouponId.generate(), "WELCOME10", PromotionId.generate(), T2, T1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void activarYDesactivarCambiaElEstado() {
        Coupon coupon = Cupon.cupon("WELCOME10");

        coupon.deactivate();
        assertThat(coupon.isActiveAt(T1)).isFalse();

        coupon.activate();
        assertThat(coupon.isActiveAt(T1)).isTrue();
    }

    private static final class Cupon {
        private Cupon() {
        }

        static Coupon cupon(String code) {
            return Coupon.create(CouponId.generate(), code, PromotionId.generate(), T1, T2);
        }
    }
}