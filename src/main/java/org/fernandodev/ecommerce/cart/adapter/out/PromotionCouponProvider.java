package org.fernandodev.ecommerce.cart.adapter.out;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.cart.application.port.out.CouponProvider;
import org.fernandodev.ecommerce.promotion.application.dto.ApplyCouponCommand;
import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponApplication;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link CouponProvider} consultando el contexto
 * Promotions & Coupons (puerto de entrada de su capa de aplicación).
 */
@Component
@RequiredArgsConstructor
public class PromotionCouponProvider implements CouponProvider {

    private final CouponUseCases couponUseCases;

    @Override
    public Money discountFor(UUID customerId, String code, List<CouponItemDto> items) {
        List<org.fernandodev.ecommerce.promotion.application.dto.CartItemDto> promotionItems = items.stream()
                .map(item -> new org.fernandodev.ecommerce.promotion.application.dto.CartItemDto(
                        item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        CouponApplication application = couponUseCases.applyCoupon(code, customerId,
                new ApplyCouponCommand(code, promotionItems));
        return application.discount();
    }
}