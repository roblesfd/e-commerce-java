package org.fernandodev.ecommerce.promotion.application.dto;

import java.util.Optional;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Resultado de la validación de un cupón.
 *
 * @param valid    si el cupón es aplicable.
 * @param reason   motivo de no aplicabilidad (vacío si {@code valid}).
 * @param discount descuento calculado (presente si {@code valid}).
 */
public record CouponValidationResult(boolean valid, String reason, Money discount) {

    public static CouponValidationResult invalid(String reason) {
        return new CouponValidationResult(false, reason, null);
    }

    public static CouponValidationResult valid(Money discount) {
        return new CouponValidationResult(true, null, discount);
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public Optional<Money> getDiscount() {
        return Optional.ofNullable(discount);
    }
}