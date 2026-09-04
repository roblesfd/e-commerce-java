package org.fernandodev.ecommerce.promotion.domain;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa un cupón: código canjeable vinculado a una
 * promoción.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Coupon {

    private CouponId id;
    private String code;
    private PromotionId promotionId;
    private boolean active;
    private Instant validFrom;
    private Instant validTo;

    /**
     * Factoría del agregado.
     */
    public static Coupon create(CouponId id, String code, PromotionId promotionId,
                                Instant validFrom, Instant validTo) {
        return crear(id, code, promotionId, true, validFrom, validTo);
    }

    /**
     * Factoría del agregado.
     */
    public static Coupon crear(CouponId id, String code, PromotionId promotionId,
                               boolean active, Instant validFrom, Instant validTo) {
        Coupon coupon = new Coupon();
        coupon.id = Objects.requireNonNull(id, "id no puede ser null");
        coupon.code = requireCode(code);
        coupon.promotionId = Objects.requireNonNull(promotionId, "promotionId no puede ser null");
        coupon.active = active;
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");
        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException("validFrom debe ser anterior a validTo");
        }
        coupon.validFrom = validFrom;
        coupon.validTo = validTo;
        return coupon;
    }

    public boolean isActiveAt(Instant at) {
        return active && !at.isBefore(validFrom) && at.isBefore(validTo);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void setValidity(Instant validFrom, Instant validTo) {
        Objects.requireNonNull(validFrom, "validFrom no puede ser null");
        Objects.requireNonNull(validTo, "validTo no puede ser null");
        if (!validFrom.isBefore(validTo)) {
            throw new BusinessRuleViolationException("validFrom debe ser anterior a validTo");
        }
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    private static String requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleViolationException("El código del cupón es obligatorio");
        }
        if (code.length() > 64) {
            throw new BusinessRuleViolationException("El código del cupón es demasiado largo");
        }
        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }
}