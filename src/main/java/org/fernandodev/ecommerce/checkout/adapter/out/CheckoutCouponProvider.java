package org.fernandodev.ecommerce.checkout.adapter.out;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.checkout.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link CouponProvider} consultando el contexto
 * Promotions &amp; Coupons.
 */
@Component
@RequiredArgsConstructor
public class CheckoutCouponProvider implements CouponProvider {

    private final CouponUseCases couponUseCases;

    @Override
    public Money discountFor(UUID customerId, String code, List<CouponItemDto> items) {
        List<org.fernandodev.ecommerce.promotion.application.dto.CartItemDto> promotionItems = items.stream()
                .map(item -> new org.fernandodev.ecommerce.promotion.application.dto.CartItemDto(
                        item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        return couponUseCases.applyCoupon(code, customerId,
                new org.fernandodev.ecommerce.promotion.application.dto.ApplyCouponCommand(code, promotionItems))
                .discount();
    }
}